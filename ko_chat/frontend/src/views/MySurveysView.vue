<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  fetchMySurveys,
  getMySurveyDetail,
  submitMySurveyResponse,
} from '../api/surveyApi'
import type { MySurveyItem } from '../api/surveyApi'
import type { SurveyDetail, SubmitSurveyResponseRequest } from '../types/survey'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { accessToken } = useAuth()
const surveys = ref<MySurveyItem[]>([])
const loading = ref(false)
const selectedSurvey = ref<SurveyDetail | null>(null)
const submitting = ref(false)
const answers = ref<Record<number, { optionIds: number[]; textAnswer: string }>>({})
const successMessage = ref('')
const errorMessage = ref('')

const pendingSurveys = computed(() => surveys.value.filter((s) => !s.hasResponded))
const completedSurveys = computed(() => surveys.value.filter((s) => s.hasResponded))

const loadSurveys = async () => {
  if (!accessToken.value) return
  loading.value = true
  try {
    surveys.value = await fetchMySurveys(accessToken.value)
  } catch (e) {
    errorMessage.value = '설문 목록을 불러올 수 없습니다'
  } finally {
    loading.value = false
  }
}

const openSurvey = async (surveyId: number) => {
  if (!accessToken.value) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    selectedSurvey.value = await getMySurveyDetail(accessToken.value, surveyId)
    answers.value = {}
    selectedSurvey.value.questions.forEach((q) => {
      answers.value[q.id] = { optionIds: [], textAnswer: '' }
    })
  } catch (e) {
    errorMessage.value = '설문 상세를 불러올 수 없습니다'
  }
}

const closeSurveyDetail = () => {
  selectedSurvey.value = null
}

const handleSubmit = async () => {
  if (!accessToken.value || !selectedSurvey.value) return
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
    await submitMySurveyResponse(accessToken.value, selectedSurvey.value.id, request)
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

      <div v-if="selectedSurvey.hasResponded" class="already-responded">
        이미 응답을 완료한 설문입니다.
      </div>

      <form v-else @submit.prevent="handleSubmit">
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
                @change="toggleOption(question.id, opt.id, true)"
              />
              <input
                v-else
                type="checkbox"
                :checked="answers[question.id]?.optionIds.includes(opt.id)"
                @change="toggleOption(question.id, opt.id, false)"
              />
              <span>{{ opt.optionText }}</span>
            </label>
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="submitting">
          {{ submitting ? '제출 중...' : '응답 제출' }}
        </button>
      </form>
    </div>

    <div v-else class="survey-list-container">
      <section v-if="pendingSurveys.length > 0" class="survey-section">
        <h2>참여 대기 설문</h2>
        <div class="survey-cards">
          <div
            v-for="s in pendingSurveys"
            :key="s.surveyId"
            class="survey-card pending"
            @click="openSurvey(s.surveyId)"
          >
            <span class="badge badge--pending">대기</span>
            <h3>{{ s.title }}</h3>
            <p v-if="s.description">{{ s.description }}</p>
            <p v-if="s.chatRoomName" class="survey-room">{{ s.chatRoomName }}</p>
          </div>
        </div>
      </section>

      <section v-if="completedSurveys.length > 0" class="survey-section">
        <h2>완료한 설문</h2>
        <div class="survey-cards">
          <div
            v-for="s in completedSurveys"
            :key="s.surveyId"
            class="survey-card completed"
            @click="openSurvey(s.surveyId)"
          >
            <span class="badge badge--completed">완료</span>
            <h3>{{ s.title }}</h3>
            <p v-if="s.description">{{ s.description }}</p>
          </div>
        </div>
      </section>

      <p v-if="!loading && surveys.length === 0" class="empty-state">
        배정된 설문이 없습니다.
      </p>
      <p v-if="loading" class="loading">불러오는 중...</p>
    </div>
  </div>
</template>

<style scoped src="../styles/views/MySurveysView.css"></style>
