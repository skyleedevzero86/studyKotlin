<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  closeSurvey,
  createSurvey,
  deleteSurvey,
  exportSurveyStatisticsExcel,
  exportSurveyStatisticsPdf,
  getSurvey,
  getSurveyStatistics,
  listRoomSurveys,
  publishSurvey,
  submitSurveyResponse,
  updateSurvey,
  uploadSurveyParticipants,
} from '../api/surveyApi'
import { getChatRoomMembers } from '../api/chatApi'
import PaginationBar from './PaginationBar.vue'
import StatisticsBarChart from './StatisticsBarChart.vue'
import SurveyParticipantUploadModal from './SurveyParticipantUploadModal.vue'
import { usePagination } from '../composables/usePagination'
import type { ChatRoomMember } from '../types/chat'
import type {
  CreateSurveyRequest,
  QuestionType,
  SurveyDetail,
  SurveyQuestionRequest,
  SurveyStatistics,
  SurveySummary,
  TargetMode,
  ParticipantUploadResult,
} from '../types/survey'
import { resolveApiError } from '../utils/resolveApiError'
import { paginateArray } from '../utils/paginateArray'
import {
  canSubmitSurveyResponse,
  getSurveyResponseBlockReason,
  surveyResponseBlockMessage,
} from '../utils/surveyResponseGuard'

const props = defineProps<{
  token: string
  roomId: number
  canManage: boolean
  currentUserId: number
}>()

const emit = defineEmits<{
  close: []
  notice: [message: string]
  error: [message: string]
}>()

type PanelTab = 'list' | 'form' | 'respond' | 'stats'

const activeTab = ref<PanelTab>('list')
const surveys = ref<SurveySummary[]>([])
const selectedSurvey = ref<SurveyDetail | null>(null)
const statistics = ref<SurveyStatistics | null>(null)
const members = ref<ChatRoomMember[]>([])
const isLoading = ref(false)
const isSaving = ref(false)
const isExporting = ref(false)
const isUploading = ref(false)
const showUploadModal = ref(false)
const uploadTargetSurveyId = ref<number | null>(null)
const uploadResult = ref<ParticipantUploadResult | null>(null)
const statsSurveyId = ref<number | null>(null)
const statsTab = ref<'by-question' | 'by-participant'>('by-question')
const surveyPagination = usePagination(10)
const memberPagination = usePagination(20)
const participantStatsPagination = usePagination(10)

const responseAnswers = reactive<Record<number, { optionIds: number[]; textAnswer: string }>>({})

const form = reactive({
  title: '',
  description: '',
  targetMode: 'ALL_MEMBERS' as TargetMode,
  randomTargetCount: 5,
  targetUserIds: [] as number[],
  startAt: '',
  endAt: '',
  questions: [
  ] as SurveyQuestionRequest[],
})

const editingSurveyId = ref<number | null>(null)

const statusLabel = (status: string, startAt?: string | null, endAt?: string | null) => {
  const now = Date.now()
  if (status === 'ACTIVE' && startAt && new Date(startAt).getTime() > now) {
    return '시작 대기'
  }
  if (status === 'ACTIVE' && endAt && new Date(endAt).getTime() < now) {
    return '종료'
  }
  switch (status) {
    case 'DRAFT': return '작성중'
    case 'ACTIVE': return '진행중'
    case 'CLOSED': return '종료'
    default: return status
  }
}

const targetModeLabel = (mode: string) => {
  switch (mode) {
    case 'ALL_MEMBERS': return '전체 멤버'
    case 'SELECTED': return '선택 멤버'
    case 'RANDOM': return '랜덤 배정'
    default: return mode
  }
}

const questionTypeLabel = (type: QuestionType) => {
  switch (type) {
    case 'SINGLE_CHOICE': return '단일선택'
    case 'MULTIPLE_CHOICE': return '복수선택'
    case 'TEXT': return '주관식'
    default: return type
  }
}

const resetForm = () => {
  form.title = ''
  form.description = ''
  form.targetMode = 'ALL_MEMBERS'
  form.randomTargetCount = 5
  form.targetUserIds = []
  form.startAt = ''
  form.endAt = ''
  form.questions = [
    {
      questionText: '',
      questionType: 'SINGLE_CHOICE',
      options: [{ optionText: '' }, { optionText: '' }],
    },
  ]
  editingSurveyId.value = null
}

const addQuestion = () => {
  form.questions.push({
    questionText: '',
    questionType: 'SINGLE_CHOICE',
    options: [{ optionText: '' }, { optionText: '' }],
  })
}

const removeQuestion = (index: number) => {
  if (form.questions.length <= 1) return
  form.questions.splice(index, 1)
}

const addOption = (questionIndex: number) => {
  form.questions[questionIndex].options.push({ optionText: '' })
}

const removeOption = (questionIndex: number, optionIndex: number) => {
  if (form.questions[questionIndex].options.length <= 2) return
  form.questions[questionIndex].options.splice(optionIndex, 1)
}

const toIsoDateTime = (value: string): string | null => {
  if (!value.trim()) return null
  return value.length === 16 ? `${value}:00` : value
}

const toDateTimeLocal = (value: string | null | undefined): string => {
  if (!value) return ''
  return value.slice(0, 16)
}

const buildRequest = (): CreateSurveyRequest => ({
  title: form.title.trim(),
  description: form.description.trim() || null,
  targetMode: form.targetMode,
  randomTargetCount: form.targetMode === 'RANDOM' ? form.randomTargetCount : null,
  startAt: toIsoDateTime(form.startAt),
  endAt: toIsoDateTime(form.endAt),
  questions: form.questions.map((q) => ({
    questionText: q.questionText.trim(),
    questionType: q.questionType,
    options: q.questionType === 'TEXT' ? [] : q.options.filter((o) => o.optionText.trim()),
  })),
  targetUserIds:
    form.targetMode === 'SELECTED' || form.targetMode === 'RANDOM'
      ? form.targetUserIds
      : [],
})

const shufflePickIds = (ids: number[], count: number): number[] => {
  const pool = [...ids]
  for (let i = pool.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[pool[i], pool[j]] = [pool[j], pool[i]]
  }
  return pool.slice(0, Math.min(count, pool.length))
}

const loadAllMemberIds = async (): Promise<number[]> => {
  const page = await getChatRoomMembers(props.token, props.roomId, 0, 500)
  return page.content.map((member) => member.user.id)
}

const randomSelectMembers = async () => {
  const requested = Number(form.randomTargetCount)
  if (!Number.isFinite(requested) || requested < 0) {
    emit('error', '랜덤 대상자 수는 0 이상 입력해 주세요.')
    return
  }
  if (requested === 0) {
    form.targetUserIds = []
    return
  }
  try {
    const memberIds = await loadAllMemberIds()
    form.targetUserIds = shufflePickIds(memberIds, requested)
  } catch (error) {
    emit('error', resolveApiError(error, '멤버 목록을 불러오지 못했습니다.'))
  }
}

const loadSurveys = async () => {
  isLoading.value = true
  try {
    const page = await listRoomSurveys(
      props.token,
      props.roomId,
      props.canManage,
      surveyPagination.page.value,
      surveyPagination.size.value,
    )
    surveys.value = page.content
    surveyPagination.applyPageResponse(page)
  } catch (error) {
    emit('error', resolveApiError(error, '설문 목록을 불러오지 못했습니다.'))
  } finally {
    isLoading.value = false
  }
}

const loadMembers = async () => {
  try {
    const page = await getChatRoomMembers(
      props.token,
      props.roomId,
      memberPagination.page.value,
      memberPagination.size.value,
    )
    members.value = page.content
    memberPagination.applyPageResponse(page)
  } catch {
    members.value = []
  }
}

const openCreate = () => {
  resetForm()
  activeTab.value = 'form'
}

const openEdit = async (surveyId: number) => {
  isLoading.value = true
  try {
    const detail = await getSurvey(props.token, props.roomId, surveyId)
    if (detail.status !== 'DRAFT') {
      emit('error', '작성중인 설문만 수정할 수 있습니다.')
      return
    }
    editingSurveyId.value = surveyId
    form.title = detail.title
    form.description = detail.description ?? ''
    form.targetMode = detail.targetMode
    form.randomTargetCount = detail.randomTargetCount ?? 5
    form.startAt = toDateTimeLocal(detail.startAt)
    form.endAt = toDateTimeLocal(detail.endAt)
    form.targetUserIds = detail.participants.map((p) => p.userId)
    form.questions = detail.questions.map((q) => ({
      questionText: q.questionText,
      questionType: q.questionType,
      options: q.options.length > 0
        ? q.options.map((o) => ({ optionText: o.optionText }))
        : [{ optionText: '' }, { optionText: '' }],
    }))
    activeTab.value = 'form'
  } catch (error) {
    emit('error', resolveApiError(error, '설문을 불러오지 못했습니다.'))
  } finally {
    isLoading.value = false
  }
}

const saveSurvey = async () => {
  isSaving.value = true
  try {
    const request = buildRequest()
    if (editingSurveyId.value) {
      await updateSurvey(props.token, props.roomId, editingSurveyId.value, request)
      emit('notice', '설문이 저장되었습니다.')
    } else {
      await createSurvey(props.token, props.roomId, request)
      emit('notice', '설문이 등록되었습니다.')
    }
    resetForm()
    activeTab.value = 'list'
    await loadSurveys()
  } catch (error) {
    emit('error', resolveApiError(error, '설문 저장에 실패했습니다.'))
  } finally {
    isSaving.value = false
  }
}

const handlePublish = async (surveyId: number) => {
  if (!confirm('설문을 게시하시겠습니까?')) return
  try {
    await publishSurvey(props.token, props.roomId, surveyId)
    emit('notice', '설문이 게시되었습니다.')
    await loadSurveys()
  } catch (error) {
    emit('error', resolveApiError(error, '설문 게시에 실패했습니다.'))
  }
}

const handleClose = async (surveyId: number) => {
  if (!confirm('설문을 종료하시겠습니까?')) return
  try {
    await closeSurvey(props.token, props.roomId, surveyId)
    emit('notice', '설문이 종료되었습니다.')
    await loadSurveys()
  } catch (error) {
    emit('error', resolveApiError(error, '설문 종료에 실패했습니다.'))
  }
}

const handleDelete = async (surveyId: number) => {
  if (!confirm('설문을 삭제하시겠습니까?')) return
  try {
    await deleteSurvey(props.token, props.roomId, surveyId)
    emit('notice', '설문이 삭제되었습니다.')
    await loadSurveys()
  } catch (error) {
    emit('error', resolveApiError(error, '설문 삭제에 실패했습니다.'))
  }
}

const openRespond = async (surveyId: number) => {
  isLoading.value = true
  try {
    const detail = await getSurvey(props.token, props.roomId, surveyId)
    if (!canSubmitSurveyResponse(detail)) {
      const reason = getSurveyResponseBlockReason(detail)
      emit(
        'error',
        reason
          ? surveyResponseBlockMessage(reason, {
              startAt: detail.startAt,
              endAt: detail.endAt,
            })
          : '설문에 응답할 수 없습니다.',
      )
      return
    }
    selectedSurvey.value = detail
    detail.questions.forEach((q) => {
      responseAnswers[q.id] = { optionIds: [], textAnswer: '' }
    })
    activeTab.value = 'respond'
  } catch (error) {
    emit('error', resolveApiError(error, '설문을 불러오지 못했습니다.'))
  } finally {
    isLoading.value = false
  }
}

const toggleOption = (questionId: number, optionId: number, questionType: QuestionType) => {
  if (!selectedSurvey.value || !canSubmitSurveyResponse(selectedSurvey.value)) return
  const current = responseAnswers[questionId]
  if (!current) return
  if (questionType === 'SINGLE_CHOICE') {
    current.optionIds = [optionId]
  } else if (questionType === 'MULTIPLE_CHOICE') {
    const idx = current.optionIds.indexOf(optionId)
    if (idx >= 0) {
      current.optionIds.splice(idx, 1)
    } else {
      current.optionIds.push(optionId)
    }
  }
}

const submitResponse = async () => {
  if (!selectedSurvey.value) return
  if (!canSubmitSurveyResponse(selectedSurvey.value)) {
    const reason = getSurveyResponseBlockReason(selectedSurvey.value)
    emit(
      'error',
      reason
        ? surveyResponseBlockMessage(reason, {
            startAt: selectedSurvey.value.startAt,
            endAt: selectedSurvey.value.endAt,
          })
        : '설문에 응답할 수 없습니다.',
    )
    return
  }
  isSaving.value = true
  try {
    const answers = selectedSurvey.value.questions.map((q) => ({
      questionId: q.id,
      optionIds: responseAnswers[q.id]?.optionIds ?? [],
      textAnswer: responseAnswers[q.id]?.textAnswer || null,
    }))
    await submitSurveyResponse(props.token, props.roomId, selectedSurvey.value.id, { answers })
    emit('notice', '설문 응답이 제출되었습니다.')
    selectedSurvey.value = null
    activeTab.value = 'list'
    await loadSurveys()
  } catch (error) {
    emit('error', resolveApiError(error, '응답 제출에 실패했습니다.'))
  } finally {
    isSaving.value = false
  }
}

const openStats = async (surveyId: number) => {
  isLoading.value = true
  try {
    statistics.value = await getSurveyStatistics(props.token, props.roomId, surveyId)
    statsSurveyId.value = surveyId
    activeTab.value = 'stats'
  } catch (error) {
    emit('error', resolveApiError(error, '통계를 불러오지 못했습니다.'))
  } finally {
    isLoading.value = false
  }
}

const handleExportExcel = async () => {
  if (!statsSurveyId.value) return
  isExporting.value = true
  try {
    await exportSurveyStatisticsExcel(props.token, props.roomId, statsSurveyId.value)
    emit('notice', 'Excel 파일을 다운로드했습니다.')
  } catch (error) {
    emit('error', resolveApiError(error, 'Excel 다운로드에 실패했습니다.'))
  } finally {
    isExporting.value = false
  }
}

const handleExportPdf = async () => {
  if (!statsSurveyId.value) return
  isExporting.value = true
  try {
    await exportSurveyStatisticsPdf(props.token, props.roomId, statsSurveyId.value)
    emit('notice', 'PDF 파일을 다운로드했습니다.')
  } catch (error) {
    emit('error', resolveApiError(error, 'PDF 다운로드에 실패했습니다.'))
  } finally {
    isExporting.value = false
  }
}

const openUploadModal = (surveyId: number) => {
  uploadTargetSurveyId.value = surveyId
  uploadResult.value = null
  showUploadModal.value = true
}

const handleUploadParticipants = async (file: File) => {
  if (!uploadTargetSurveyId.value) return
  isUploading.value = true
  try {
    uploadResult.value = await uploadSurveyParticipants(
      props.token,
      props.roomId,
      uploadTargetSurveyId.value,
      file,
    )
    emit('notice', `참여자 ${uploadResult.value.successCount}명이 업로드되었습니다.`)
    await loadSurveys()
  } catch (error) {
    emit('error', resolveApiError(error, '참여자 업로드에 실패했습니다.'))
  } finally {
    isUploading.value = false
  }
}

const chartLabels = computed(() =>
  statistics.value?.byQuestion.map((q) => `${q.questionNo}. ${q.questionText}`) ?? [],
)

const chartValues = computed(() =>
  statistics.value?.byQuestion.map((q) =>
    q.options.reduce((sum, o) => sum + (o.selectCount ?? 0), 0),
  ) ?? [],
)

const pagedParticipants = computed(() => {
  if (!statistics.value) return []
  return paginateArray(
    statistics.value.byParticipant,
    participantStatsPagination.page.value,
    participantStatsPagination.size.value,
  )
})

watch(
  () => statistics.value?.byParticipant.length ?? 0,
  (count) => {
    participantStatsPagination.totalElements.value = count
    participantStatsPagination.totalPages.value = Math.max(1, Math.ceil(count / participantStatsPagination.size.value))
  },
)

const toggleTargetUser = (userId: number) => {
  const idx = form.targetUserIds.indexOf(userId)
  if (idx >= 0) {
    form.targetUserIds.splice(idx, 1)
  } else {
    form.targetUserIds.push(userId)
  }
}

watch(
  () => props.roomId,
  () => {
    surveyPagination.resetPage()
    memberPagination.resetPage()
    void loadSurveys()
    void loadMembers()
  },
)

watch(() => surveyPagination.page.value, () => {
  void loadSurveys()
})

watch(() => memberPagination.page.value, () => {
  void loadMembers()
})

onMounted(() => {
  void loadSurveys()
  void loadMembers()
})
</script>

<template>
  <div class="survey-panel">
    <header class="survey-panel-header">
      <h3>설문조사</h3>
      <button type="button" class="survey-close" @click="emit('close')">✕</button>
    </header>

    <nav class="survey-tabs">
      <button
        type="button"
        :class="{ active: activeTab === 'list' }"
        @click="activeTab = 'list'"
      >
        목록
      </button>
      <button
        v-if="canManage"
        type="button"
        :class="{ active: activeTab === 'form' }"
        @click="openCreate"
      >
        {{ editingSurveyId ? '수정' : '새 설문' }}
      </button>
      <button
        v-if="statistics"
        type="button"
        :class="{ active: activeTab === 'stats' }"
        @click="activeTab = 'stats'"
      >
        통계
      </button>
    </nav>

    <p v-if="isLoading" class="chat-empty slim">불러오는 중...</p>

    <section v-else-if="activeTab === 'list'" class="survey-section">
      <div v-if="canManage" class="survey-actions-top">
        <button type="button" class="button-primary" @click="openCreate">+ 설문 만들기</button>
      </div>
      <p v-if="surveys.length === 0" class="chat-empty slim">등록된 설문이 없습니다.</p>
      <article v-for="survey in surveys" :key="survey.id" class="survey-card">
        <div class="survey-card-head">
          <strong>{{ survey.title }}</strong>
          <span class="survey-badge" :data-status="survey.status">
            {{ statusLabel(survey.status, survey.startAt, survey.endAt) }}
          </span>
        </div>
        <p v-if="survey.description" class="survey-desc">{{ survey.description }}</p>
        <div class="survey-meta">
          <span>{{ targetModeLabel(survey.targetMode) }}</span>
          <span>문항 {{ survey.questionCount }}</span>
          <span>응답 {{ survey.completedCount }}/{{ survey.participantCount || '-' }}</span>
        </div>
        <div class="survey-card-actions">
          <button
            v-if="survey.status === 'ACTIVE' && survey.canRespond"
            type="button"
            @click="openRespond(survey.id)"
          >
            응답하기
          </button>
          <span v-else-if="survey.waitingForStart" class="survey-responded">시작 대기</span>
          <span v-else-if="survey.hasResponded" class="survey-responded">응답 완료</span>
          <span v-else-if="survey.status === 'CLOSED' || survey.status === 'ACTIVE'" class="survey-responded">
            종료
          </span>
          <button v-if="canManage" type="button" @click="openStats(survey.id)">통계</button>
          <button
            v-if="canManage && (survey.status === 'DRAFT' || survey.status === 'ACTIVE')"
            type="button"
            @click="openUploadModal(survey.id)"
          >
            대상자 업로드
          </button>
          <button
            v-if="canManage && survey.status === 'DRAFT'"
            type="button"
            @click="openEdit(survey.id)"
          >
            수정
          </button>
          <button
            v-if="canManage && survey.status === 'DRAFT'"
            type="button"
            @click="handlePublish(survey.id)"
          >
            게시
          </button>
          <button
            v-if="canManage && survey.status === 'ACTIVE'"
            type="button"
            @click="handleClose(survey.id)"
          >
            종료
          </button>
          <button
            v-if="canManage && survey.status === 'DRAFT'"
            type="button"
            class="danger"
            @click="handleDelete(survey.id)"
          >
            삭제
          </button>
        </div>
      </article>
      <PaginationBar
        v-if="surveys.length || surveyPagination.totalElements.value > 0"
        :page="surveyPagination.page.value"
        :total-pages="surveyPagination.totalPages.value"
        :total-elements="surveyPagination.totalElements.value"
        :has-prev="surveyPagination.hasPrev.value"
        :has-next="surveyPagination.hasNext.value"
        :page-label="surveyPagination.pageLabel.value"
        @prev="surveyPagination.goPrev()"
        @next="surveyPagination.goNext()"
      />
    </section>

    <section v-else-if="activeTab === 'form' && canManage" class="survey-section survey-form">
      <label>
        설문 제목
        <input v-model="form.title" type="text" maxlength="200" required />
      </label>
      <label>
        안내 문구
        <textarea v-model="form.description" rows="3" maxlength="2000" />
      </label>
      <label>
        시작 일시
        <input v-model="form.startAt" type="datetime-local" />
      </label>
      <label>
        종료 일시
        <input v-model="form.endAt" type="datetime-local" />
      </label>
      <label>
        대상자
        <select v-model="form.targetMode">
          <option value="ALL_MEMBERS">전체 멤버</option>
          <option value="SELECTED">선택 멤버</option>
          <option value="RANDOM">랜덤 배정</option>
        </select>
      </label>
      <label v-if="form.targetMode === 'RANDOM'" class="survey-random-row">
        랜덤 인원
        <input v-model.number="form.randomTargetCount" type="number" min="0" />
        <button type="button" @click="randomSelectMembers">랜덤 배정</button>
      </label>
      <p v-if="form.targetMode === 'RANDOM'" class="survey-random-summary">
        배정됨: {{ form.targetUserIds.length }}명
      </p>
      <div v-if="form.targetMode === 'SELECTED'" class="survey-target-list">
        <p>대상 멤버 선택</p>
        <label v-for="member in members" :key="member.user.id" class="survey-target-item">
          <input
            type="checkbox"
            :checked="form.targetUserIds.includes(member.user.id)"
            @change="toggleTargetUser(member.user.id)"
          />
          {{ member.user.displayName ?? member.user.username }}
        </label>
        <PaginationBar
          v-if="members.length || memberPagination.totalElements.value > 0"
          :page="memberPagination.page.value"
          :total-pages="memberPagination.totalPages.value"
          :total-elements="memberPagination.totalElements.value"
          :has-prev="memberPagination.hasPrev.value"
          :has-next="memberPagination.hasNext.value"
          :page-label="memberPagination.pageLabel.value"
          @prev="memberPagination.goPrev()"
          @next="memberPagination.goNext()"
        />
      </div>

      <div v-for="(question, qi) in form.questions" :key="qi" class="survey-question-block">
        <div class="survey-question-head">
          <strong>문항 {{ qi + 1 }}</strong>
          <button type="button" class="danger" @click="removeQuestion(qi)">삭제</button>
        </div>
        <label>
          유형
          <select v-model="question.questionType">
            <option value="SINGLE_CHOICE">단일선택</option>
            <option value="MULTIPLE_CHOICE">복수선택</option>
            <option value="TEXT">주관식</option>
          </select>
        </label>
        <label>
          질문
          <input v-model="question.questionText" type="text" maxlength="500" required />
        </label>
        <div v-if="question.questionType !== 'TEXT'" class="survey-options">
          <div v-for="(option, oi) in question.options" :key="oi" class="survey-option-row">
            <input v-model="option.optionText" type="text" placeholder="보기" maxlength="300" />
            <button type="button" @click="removeOption(qi, oi)">−</button>
          </div>
          <button type="button" @click="addOption(qi)">+ 보기 추가</button>
        </div>
      </div>

      <div class="survey-form-actions">
        <button type="button" @click="addQuestion">+ 문항 추가</button>
        <button type="button" class="button-primary" :disabled="isSaving" @click="saveSurvey">
          {{ isSaving ? '저장 중...' : '저장' }}
        </button>
        <button type="button" @click="activeTab = 'list'">취소</button>
      </div>
    </section>

    <section v-else-if="activeTab === 'respond' && selectedSurvey" class="survey-section">
      <h4>{{ selectedSurvey.title }}</h4>
      <p v-if="selectedSurvey.description" class="survey-desc">{{ selectedSurvey.description }}</p>

      <p v-if="selectedSurvey && getSurveyResponseBlockReason(selectedSurvey)" class="survey-responded">
        {{
          surveyResponseBlockMessage(getSurveyResponseBlockReason(selectedSurvey)!, {
            startAt: selectedSurvey.startAt,
            endAt: selectedSurvey.endAt,
          })
        }}
      </p>

      <template v-else-if="selectedSurvey && canSubmitSurveyResponse(selectedSurvey)">
      <div v-for="question in selectedSurvey.questions" :key="question.id" class="survey-respond-block">
        <p class="survey-question-title">
          {{ question.questionNo }}. {{ question.questionText }}
          <span class="survey-type-tag">{{ questionTypeLabel(question.questionType) }}</span>
        </p>
        <div v-if="question.questionType === 'TEXT'">
          <textarea
            v-model="responseAnswers[question.id].textAnswer"
            rows="3"
            placeholder="답변을 입력하세요"
            :disabled="!canSubmitSurveyResponse(selectedSurvey)"
            :readonly="!canSubmitSurveyResponse(selectedSurvey)"
          />
        </div>
        <div v-else class="survey-option-list">
          <label
            v-for="option in question.options"
            :key="option.id"
            class="survey-option-choice"
          >
            <input
              :type="question.questionType === 'SINGLE_CHOICE' ? 'radio' : 'checkbox'"
              :name="`q-${question.id}`"
              :checked="responseAnswers[question.id]?.optionIds.includes(option.id)"
              :disabled="!canSubmitSurveyResponse(selectedSurvey)"
              @change="toggleOption(question.id, option.id, question.questionType)"
            />
            {{ option.optionText }}
          </label>
        </div>
      </div>
      <div class="survey-form-actions">
        <button
          type="button"
          class="button-primary"
          :disabled="isSaving || !canSubmitSurveyResponse(selectedSurvey)"
          @click="submitResponse"
        >
          {{ isSaving ? '제출 중...' : '제출' }}
        </button>
        <button type="button" @click="activeTab = 'list'">취소</button>
      </div>
      </template>
    </section>

    <section v-else-if="activeTab === 'stats' && statistics" class="survey-section">
      <h4>{{ statistics.title }} 통계</h4>
      <p class="survey-meta">
        참여 {{ statistics.completedParticipants }} / {{ statistics.totalParticipants }}
      </p>
      <div class="survey-export-actions">
        <button type="button" :disabled="isExporting" @click="handleExportExcel">Excel</button>
        <button type="button" :disabled="isExporting" @click="handleExportPdf">PDF</button>
      </div>
      <nav class="survey-stats-tabs">
        <button
          type="button"
          :class="{ active: statsTab === 'by-question' }"
          @click="statsTab = 'by-question'"
        >
          문항별
        </button>
        <button
          type="button"
          :class="{ active: statsTab === 'by-participant' }"
          @click="statsTab = 'by-participant'"
        >
          참여자별
        </button>
      </nav>

      <div v-if="statsTab === 'by-question'">
        <StatisticsBarChart
          v-if="chartLabels.length > 0"
          :labels="chartLabels"
          :values="chartValues"
        />
        <div v-for="question in statistics.byQuestion" :key="question.questionId" class="survey-stat-block">
          <strong>{{ question.questionNo }}. {{ question.questionText }}</strong>
          <p>응답자 {{ question.respondentCount }}명</p>
          <ul v-if="question.options.length > 0">
            <li v-for="option in question.options" :key="option.id">
              {{ option.optionText }}: {{ option.selectCount ?? 0 }}표
            </li>
          </ul>
          <ul v-else>
            <li v-for="(answer, idx) in question.textAnswers" :key="idx">{{ answer }}</li>
          </ul>
        </div>
      </div>

      <div v-else>
        <div
          v-for="participant in pagedParticipants"
          :key="participant.userId"
          class="survey-stat-block"
        >
          <strong>{{ participant.displayName ?? participant.username }}</strong>
          <span class="survey-badge" :data-status="participant.status">
            {{ participant.status === 'COMPLETED' ? '완료' : '대기' }}
          </span>
          <ul>
            <li v-for="answer in participant.answers" :key="answer.questionId">
              {{ answer.questionText }}:
              <template v-if="answer.textAnswer">{{ answer.textAnswer }}</template>
              <template v-else>{{ answer.optionTexts.join(', ') }}</template>
            </li>
          </ul>
        </div>
        <PaginationBar
          v-if="statistics.byParticipant.length"
          :page="participantStatsPagination.page.value"
          :total-pages="participantStatsPagination.totalPages.value"
          :total-elements="participantStatsPagination.totalElements.value"
          :has-prev="participantStatsPagination.hasPrev.value"
          :has-next="participantStatsPagination.hasNext.value"
          :page-label="participantStatsPagination.pageLabel.value"
          @prev="participantStatsPagination.goPrev()"
          @next="participantStatsPagination.goNext()"
        />
      </div>
    </section>
  </div>

  <SurveyParticipantUploadModal
    :open="showUploadModal"
    :uploading="isUploading"
    :result="uploadResult"
    @close="showUploadModal = false"
    @upload="handleUploadParticipants"
  />
</template>

<style scoped src="../styles/components/SurveyPanel.css"></style>
