<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  fetchMySurveys,
  getMySurveyDetail,
  submitMySurveyResponse,
} from '../api/surveyApi'
import type { MySurveyItem } from '../api/surveyApi'
import type { SurveyDetail, SubmitSurveyResponseRequest } from '../types/survey'
import PaginationBar from '../components/PaginationBar.vue'
import { useAuth } from '../composables/useAuth'
import { usePagination } from '../composables/usePagination'
import { useSurveyNotification } from '../composables/useSurveyNotification'
import { resolveApiError } from '../utils/resolveApiError'
import { paginateArray } from '../utils/paginateArray'
import {
  canSubmitSurveyResponse,
  getSurveyResponseBlockReason,
  surveyResponseBlockMessage,
} from '../utils/surveyResponseGuard'

const router = useRouter()
const { getValidAccessToken } = useAuth()
const { refreshPendingSurveyNotifications } = useSurveyNotification()
const pendingPagination = usePagination(6)
const completedPagination = usePagination(6)
const surveys = ref<MySurveyItem[]>([])
const loading = ref(false)
const selectedSurvey = ref<SurveyDetail | null>(null)
const submitting = ref(false)
const answers = ref<Record<number, { optionIds: number[]; textAnswer: string }>>({})
const successMessage = ref('')
const errorMessage = ref('')

const pendingSurveys = computed(() => surveys.value.filter((s) => !s.hasResponded))
const completedSurveys = computed(() => surveys.value.filter((s) => s.hasResponded))

const isSurveyCardDisabled = (survey: MySurveyItem) => survey.status === 'CLOSED'

const handleSurveyCardClick = (survey: MySurveyItem) => {
  if (isSurveyCardDisabled(survey)) return
  void openSurvey(survey.surveyId)
}

const formatDateTime = (value: string | null | undefined) => {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

const selectedSurveyBlockReason = computed(() =>
  selectedSurvey.value ? getSurveyResponseBlockReason(selectedSurvey.value) : null,
)

const canSubmitSelectedSurvey = computed(() =>
  selectedSurvey.value ? canSubmitSurveyResponse(selectedSurvey.value) : false,
)

const selectedSurveyBlockMessage = computed(() => {
  if (!selectedSurvey.value || !selectedSurveyBlockReason.value) return ''
  return surveyResponseBlockMessage(selectedSurveyBlockReason.value, {
    startAt: selectedSurvey.value.startAt,
    endAt: selectedSurvey.value.endAt,
  })
})

const surveyBadgeLabel = (survey: MySurveyItem) => {
  if (survey.waitingForStart) return '시작 대기'
  if (survey.canRespond) return '참여 가능'
  if (survey.status === 'CLOSED') return '종료'
  return '대기'
}

const pagedPendingSurveys = computed(() =>
  paginateArray(
    pendingSurveys.value,
    pendingPagination.page.value,
    pendingPagination.size.value,
  ),
)

const pagedCompletedSurveys = computed(() =>
  paginateArray(
    completedSurveys.value,
    completedPagination.page.value,
    completedPagination.size.value,
  ),
)

watch(
  () => pendingSurveys.value.length,
  (count) => {
    pendingPagination.totalElements.value = count
    pendingPagination.totalPages.value = Math.max(
      1,
      Math.ceil(count / pendingPagination.size.value),
    )
    if (pendingPagination.page.value >= pendingPagination.totalPages.value) {
      pendingPagination.page.value = Math.max(0, pendingPagination.totalPages.value - 1)
    }
  },
  { immediate: true },
)

watch(
  () => completedSurveys.value.length,
  (count) => {
    completedPagination.totalElements.value = count
    completedPagination.totalPages.value = Math.max(
      1,
      Math.ceil(count / completedPagination.size.value),
    )
    if (completedPagination.page.value >= completedPagination.totalPages.value) {
      completedPagination.page.value = Math.max(0, completedPagination.totalPages.value - 1)
    }
  },
  { immediate: true },
)

const loadSurveys = async () => {
  const token = getValidAccessToken()
  if (!token) {
    errorMessage.value = '로그인이 필요합니다'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    surveys.value = await fetchMySurveys(token)
    await refreshPendingSurveyNotifications()
  } catch (e) {
    errorMessage.value = resolveApiError(e, '설문 목록을 불러올 수 없습니다')
  } finally {
    loading.value = false
  }
}

const openSurvey = async (surveyId: number) => {
  const token = getValidAccessToken()
  if (!token) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    selectedSurvey.value = await getMySurveyDetail(token, surveyId)
    answers.value = {}
    selectedSurvey.value.questions.forEach((q) => {
      answers.value[q.id] = { optionIds: [], textAnswer: '' }
    })
  } catch (e) {
    errorMessage.value = resolveApiError(e, '설문 상세를 불러올 수 없습니다')
  }
}

const closeSurveyDetail = () => {
  selectedSurvey.value = null
}

const handleSubmit = async () => {
  const token = getValidAccessToken()
  if (!token || !selectedSurvey.value) return
  if (!canSubmitSurveyResponse(selectedSurvey.value)) {
    const reason = getSurveyResponseBlockReason(selectedSurvey.value)
    errorMessage.value = reason
      ? surveyResponseBlockMessage(reason, {
          startAt: selectedSurvey.value.startAt,
          endAt: selectedSurvey.value.endAt,
        })
      : '설문에 응답할 수 없습니다.'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    const request: SubmitSurveyResponseRequest = {
      answers: selectedSurvey.value.questions.map((q) => ({
        questionId: q.id,
        optionIds: answers.value[q.id]?.optionIds ?? [],
        textAnswer: answers.value[q.id]?.textAnswer || null,
      })),
    }
    await submitMySurveyResponse(token, selectedSurvey.value.id, request)
    successMessage.value = '설문 응답이 제출되었습니다!'
    selectedSurvey.value = null
    await loadSurveys()
  } catch (e) {
    errorMessage.value = e instanceof Error ? e.message : '응답 제출에 실패했습니다'
  } finally {
    submitting.value = false
  }
}

const toggleOption = (questionId: number, optionId: number, isSingle: boolean) => {
  if (!canSubmitSelectedSurvey.value) return
  const a = answers.value[questionId]
  if (!a) return
  if (isSingle) {
    a.optionIds = [optionId]
  } else {
    const idx = a.optionIds.indexOf(optionId)
    if (idx >= 0) a.optionIds.splice(idx, 1)
    else a.optionIds.push(optionId)
  }
}

const goBack = () => {
  router.push({ name: 'home' })
}

onMounted(loadSurveys)
</script>

<template>
  <div class="my-surveys-page">
    <header class="my-surveys-header">
      <button type="button" class="back-btn" @click="goBack">← 돌아가기</button>
      <h1>내 설문조사</h1>
    </header>

    <div v-if="successMessage" class="alert alert--success">{{ successMessage }}</div>
    <div v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</div>

    <div v-if="selectedSurvey" class="survey-form-container">
      <div class="survey-form-header">
        <h2>{{ selectedSurvey.title }}</h2>
        <p v-if="selectedSurvey.description" class="survey-desc">{{ selectedSurvey.description }}</p>
        <button type="button" class="secondary" @click="closeSurveyDetail">목록으로</button>
      </div>

      <div v-if="selectedSurveyBlockReason" class="already-responded">
        {{ selectedSurveyBlockMessage }}
      </div>

      <form v-else-if="canSubmitSelectedSurvey" @submit.prevent="handleSubmit">
        <div
          v-for="question in selectedSurvey.questions"
          :key="question.id"
          class="question-card"
        >
          <h3 class="question-title">
            <span class="question-no">Q{{ question.questionNo }}.</span>
            {{ question.questionText }}
          </h3>

          <div v-if="question.questionType === 'TEXT'" class="question-body">
            <textarea
              v-model="answers[question.id].textAnswer"
              placeholder="답변을 입력하세요"
              rows="3"
              :disabled="!canSubmitSelectedSurvey"
              :readonly="!canSubmitSelectedSurvey"
            />
          </div>

          <div v-else class="question-body options">
            <label
              v-for="opt in question.options"
              :key="opt.id"
              class="option-label"
            >
              <input
                v-if="question.questionType === 'SINGLE_CHOICE'"
                type="radio"
                :name="`q-${question.id}`"
                :checked="answers[question.id]?.optionIds.includes(opt.id)"
                :disabled="!canSubmitSelectedSurvey"
                @change="toggleOption(question.id, opt.id, true)"
              />
              <input
                v-else
                type="checkbox"
                :checked="answers[question.id]?.optionIds.includes(opt.id)"
                :disabled="!canSubmitSelectedSurvey"
                @change="toggleOption(question.id, opt.id, false)"
              />
              <span>{{ opt.optionText }}</span>
            </label>
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="submitting || !canSubmitSelectedSurvey">
          {{ submitting ? '제출 중...' : '응답 제출' }}
        </button>
      </form>
    </div>

    <div v-else class="survey-list-container">
      <section v-if="pendingSurveys.length > 0" class="survey-section">
        <h2>배정된 설문 ({{ pendingSurveys.length }}건)</h2>
        <div class="survey-cards">
          <div
            v-for="s in pagedPendingSurveys"
            :key="s.surveyId"
            class="survey-card pending"
            :class="{ 'survey-card--disabled': isSurveyCardDisabled(s) }"
            @click="handleSurveyCardClick(s)"
          >
            <span
              class="badge"
              :class="{
                'badge--pending': s.waitingForStart || (!s.canRespond && s.status !== 'CLOSED'),
                'badge--open': s.canRespond,
                'badge--closed': s.status === 'CLOSED',
              }"
            >
              {{ surveyBadgeLabel(s) }}
            </span>
            <h3>{{ s.title }}</h3>
            <p v-if="s.description">{{ s.description }}</p>
            <p v-if="s.waitingForStart && s.startAt" class="survey-schedule">
              시작: {{ formatDateTime(s.startAt) }}
            </p>
            <p v-else-if="s.endAt" class="survey-schedule">종료: {{ formatDateTime(s.endAt) }}</p>
            <p v-if="s.chatRoomName" class="survey-room">{{ s.chatRoomName }}</p>
          </div>
        </div>
        <PaginationBar
          v-if="pendingSurveys.length > 0"
          :page="pendingPagination.page.value"
          :total-pages="pendingPagination.totalPages.value"
          :total-elements="pendingPagination.totalElements.value"
          :has-prev="pendingPagination.hasPrev.value"
          :has-next="pendingPagination.hasNext.value"
          :page-label="pendingPagination.pageLabel.value"
          @prev="pendingPagination.goPrev()"
          @next="pendingPagination.goNext()"
        />
      </section>

      <section v-if="completedSurveys.length > 0" class="survey-section">
        <h2>완료한 설문 ({{ completedSurveys.length }}건)</h2>
        <div class="survey-cards">
          <div
            v-for="s in pagedCompletedSurveys"
            :key="s.surveyId"
            class="survey-card completed"
            @click="openSurvey(s.surveyId)"
          >
            <span class="badge badge--completed">완료</span>
            <h3>{{ s.title }}</h3>
            <p v-if="s.description">{{ s.description }}</p>
          </div>
        </div>
        <PaginationBar
          v-if="completedSurveys.length > 0"
          :page="completedPagination.page.value"
          :total-pages="completedPagination.totalPages.value"
          :total-elements="completedPagination.totalElements.value"
          :has-prev="completedPagination.hasPrev.value"
          :has-next="completedPagination.hasNext.value"
          :page-label="completedPagination.pageLabel.value"
          @prev="completedPagination.goPrev()"
          @next="completedPagination.goNext()"
        />
      </section>

      <p v-if="!loading && surveys.length === 0" class="empty-state">
        배정된 설문이 없습니다.
      </p>
      <p v-if="loading" class="loading">불러오는 중...</p>
    </div>
  </div>
</template>

<style scoped src="../styles/views/MySurveysView.css"></style>
