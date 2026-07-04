<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminChatRooms } from '../api/chatApi'
import {
  adminAssignRandomParticipants,
  adminCloseSurvey,
  adminCreateSurvey,
  adminExportSurveyStatisticsExcel,
  adminExportSurveyStatisticsPdf,
  adminGetSurveyStatistics,
  adminListSelectableUsers,
  adminPublishSurvey,
  adminRoomStatistics,
  adminUploadParticipants,
  listAdminSurveys,
} from '../api/surveyApi'
import type { SurveyUserItem } from '../api/surveyApi'
import PaginationBar from '../components/PaginationBar.vue'
import StatisticsBarChart from '../components/StatisticsBarChart.vue'
import SurveyParticipantUploadModal from '../components/SurveyParticipantUploadModal.vue'
import { useAuth } from '../composables/useAuth'
import { usePagination } from '../composables/usePagination'
import type { ChatRoom } from '../types/chat'
import type {
  CreateSurveyRequest,
  QuestionType,
  StatisticsTab,
  SurveyStatistics,
  SurveySummary,
  TargetMode,
  ParticipantUploadResult,
} from '../types/survey'
import { resolveApiError } from '../utils/resolveApiError'
import { paginateArray } from '../utils/paginateArray'

const router = useRouter()
const { accessToken, isAdmin, getValidAccessToken } = useAuth()
const surveyPagination = usePagination(20)
const roomFilterPagination = usePagination(20)
const participantStatsPagination = usePagination(10)

const surveys = ref<SurveySummary[]>([])
const rooms = ref<ChatRoom[]>([])
const selectableUsers = ref<SurveyUserItem[]>([])
const selectedUserIds = ref<Set<number>>(new Set())
const statistics = ref<SurveyStatistics | null>(null)
const roomStats = ref<Awaited<ReturnType<typeof adminRoomStatistics>> | null>(null)
const isLoading = ref(false)
const isSaving = ref(false)
const isExporting = ref(false)
const isUploading = ref(false)
const errorMessage = ref<string | null>(null)
const showUploadModal = ref(false)
const uploadTargetSurveyId = ref<number | null>(null)
const uploadResult = ref<ParticipantUploadResult | null>(null)
const activeTab = ref<'list' | 'create' | 'stats'>('list')
const statsTab = ref<StatisticsTab>('by-question')
const selectedSurveyId = ref<number | null>(null)
const randomCount = ref(5)

const filter = reactive({
  status: '' as '' | 'DRAFT' | 'ACTIVE' | 'CLOSED',
  chatRoomId: '' as '' | number,
  targetMode: '' as '' | TargetMode,
  title: '',
  from: '',
  to: '',
})

const form = reactive({
  title: '',
  description: '',
  targetMode: 'SELECTED' as TargetMode,
  randomTargetCount: 5,
  startAt: '',
  endAt: '',
  questions: [
    {
      questionText: '',
      questionType: 'SINGLE_CHOICE' as QuestionType,
      options: [{ optionText: '' }, { optionText: '' }],
    },
  ],
})

const statusLabel = (status: string) => {
  switch (status) {
    case 'DRAFT': return '작성중'
    case 'ACTIVE': return '진행중'
    case 'CLOSED': return '종료'
    default: return status
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

const pagedRoomStats = computed(() => {
  if (!roomStats.value) return []
  return paginateArray(
    roomStats.value.rows,
    roomFilterPagination.page.value,
    roomFilterPagination.size.value,
  )
})

watch(
  () => statistics.value?.byParticipant.length ?? 0,
  (count) => {
    participantStatsPagination.totalElements.value = count
    participantStatsPagination.totalPages.value = Math.max(
      1,
      Math.ceil(count / participantStatsPagination.size.value),
    )
  },
)

watch(
  () => roomStats.value?.rows.length ?? 0,
  (count) => {
    roomFilterPagination.totalElements.value = count
    roomFilterPagination.totalPages.value = Math.max(
      1,
      Math.ceil(count / roomFilterPagination.size.value),
    )
  },
)

const toIsoDateTime = (value: string): string | null => {
  if (!value.trim()) return null
  return value.length === 16 ? `${value}:00` : value
}

const loadSurveys = async () => {
  const token = getValidAccessToken()
  if (!token) return
  isLoading.value = true
  errorMessage.value = null
  try {
    const page = await listAdminSurveys(
      token,
      surveyPagination.page.value,
      surveyPagination.size.value,
      {
        status: filter.status || undefined,
        chatRoomId: filter.chatRoomId ? Number(filter.chatRoomId) : undefined,
        targetMode: filter.targetMode || undefined,
        title: filter.title.trim() || undefined,
        from: filter.from || undefined,
        to: filter.to || undefined,
      },
    )
    surveys.value = page.content
    surveyPagination.applyPageResponse(page)
  } catch (error) {
    errorMessage.value = resolveApiError(error, '설문 목록을 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

const loadRooms = async () => {
  const token = getValidAccessToken()
  if (!token) return
  try {
    const page = await getAdminChatRooms(token, 0, 100)
    rooms.value = page.content
  } catch (error) {
    errorMessage.value = resolveApiError(error, '채팅방 목록을 불러오지 못했습니다.')
  }
}

const loadSelectableUsers = async () => {
  const token = getValidAccessToken()
  if (!token) return
  try {
    selectableUsers.value = await adminListSelectableUsers(token)
    randomSelectUsers()
  } catch (error) {
    errorMessage.value = resolveApiError(error, '회원 목록을 불러오지 못했습니다.')
  }
}

const randomSelectUsers = () => {
  const count = Math.min(form.randomTargetCount, selectableUsers.value.length)
  const shuffled = [...selectableUsers.value].sort(() => Math.random() - 0.5)
  selectedUserIds.value = new Set(shuffled.slice(0, count).map((u) => u.id))
}

const toggleUser = (userId: number) => {
  const newSet = new Set(selectedUserIds.value)
  if (newSet.has(userId)) {
    newSet.delete(userId)
  } else {
    newSet.add(userId)
  }
  selectedUserIds.value = newSet
}

const selectAllUsers = () => {
  selectedUserIds.value = new Set(selectableUsers.value.map((u) => u.id))
}

const deselectAllUsers = () => {
  selectedUserIds.value = new Set()
}

const buildRequest = (): CreateSurveyRequest => ({
  title: form.title.trim(),
  description: form.description.trim() || null,
  targetMode: form.targetMode,
  randomTargetCount: form.targetMode === 'RANDOM' ? form.randomTargetCount : null,
  targetUserIds: form.targetMode === 'SELECTED' ? [...selectedUserIds.value] : undefined,
  startAt: toIsoDateTime(form.startAt),
  endAt: toIsoDateTime(form.endAt),
  questions: form.questions.map((q) => ({
    questionText: q.questionText.trim(),
    questionType: q.questionType,
    options: q.questionType === 'TEXT' ? [] : q.options.filter((o) => o.optionText.trim()),
  })),
})

const createSurvey = async () => {
  const token = getValidAccessToken()
  if (!token) return
  if (form.targetMode === 'SELECTED' && selectedUserIds.value.size === 0) {
    errorMessage.value = '대상자를 1명 이상 선택해 주세요.'
    return
  }
  isSaving.value = true
  errorMessage.value = null
  try {
    const detail = await adminCreateSurvey(token, buildRequest())
    await adminPublishSurvey(token, detail.id)
    activeTab.value = 'list'
    await loadSurveys()
  } catch (error) {
    errorMessage.value = resolveApiError(error, '설문 생성에 실패했습니다.')
  } finally {
    isSaving.value = false
  }
}

const assignRandom = async (surveyId: number) => {
  const token = getValidAccessToken()
  if (!token) return
  try {
    await adminAssignRandomParticipants(token, surveyId, { count: randomCount.value })
    await loadSurveys()
  } catch (error) {
    errorMessage.value = resolveApiError(error, '랜덤 배정에 실패했습니다.')
  }
}

const loadStatistics = async (surveyId: number) => {
  const token = getValidAccessToken()
  if (!token) return
  isLoading.value = true
  selectedSurveyId.value = surveyId
  try {
    statistics.value = await adminGetSurveyStatistics(token, surveyId)
    roomStats.value = await adminRoomStatistics(token, surveyId)
    activeTab.value = 'stats'
    statsTab.value = 'by-question'
  } catch (error) {
    errorMessage.value = resolveApiError(error, '통계를 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

const handleExportExcel = async () => {
  const token = getValidAccessToken()
  if (!token || !selectedSurveyId.value) return
  isExporting.value = true
  try {
    await adminExportSurveyStatisticsExcel(token, selectedSurveyId.value)
  } catch (error) {
    errorMessage.value = resolveApiError(error, 'Excel 다운로드에 실패했습니다.')
  } finally {
    isExporting.value = false
  }
}

const handleExportPdf = async () => {
  const token = getValidAccessToken()
  if (!token || !selectedSurveyId.value) return
  isExporting.value = true
  try {
    await adminExportSurveyStatisticsPdf(token, selectedSurveyId.value)
  } catch (error) {
    errorMessage.value = resolveApiError(error, 'PDF 다운로드에 실패했습니다.')
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
  const token = getValidAccessToken()
  if (!token || !uploadTargetSurveyId.value) return
  isUploading.value = true
  try {
    uploadResult.value = await adminUploadParticipants(token, uploadTargetSurveyId.value, file)
    await loadSurveys()
  } catch (error) {
    errorMessage.value = resolveApiError(error, '참여자 업로드에 실패했습니다.')
  } finally {
    isUploading.value = false
  }
}

const handleAdminClose = async (surveyId: number) => {
  const token = getValidAccessToken()
  if (!token) return
  try {
    await adminCloseSurvey(token, surveyId)
    await loadSurveys()
  } catch (error) {
    errorMessage.value = resolveApiError(error, '설문 종료에 실패했습니다.')
  }
}

const resetFilters = () => {
  filter.status = ''
  filter.chatRoomId = ''
  filter.targetMode = ''
  filter.title = ''
  filter.from = ''
  filter.to = ''
  surveyPagination.resetPage()
  void loadSurveys()
}

const addQuestion = () => {
  form.questions.push({
    questionText: '',
    questionType: 'SINGLE_CHOICE',
    options: [{ optionText: '' }, { optionText: '' }],
  })
}

onMounted(async () => {
  if (!accessToken.value || !isAdmin.value) {
    await router.push({ name: 'home' })
    return
  }
  await Promise.all([loadSurveys(), loadRooms(), loadSelectableUsers()])
})

watch(() => surveyPagination.page.value, () => {
  void loadSurveys()
})
</script>

<template>
  <div class="admin-page">
    <header class="admin-header">
      <button type="button" @click="router.push({ name: 'home' })">← 돌아가기</button>
      <h1>관리자 · 설문조사</h1>
    </header>

    <nav class="admin-tabs">
      <button type="button" :class="{ active: activeTab === 'list' }" @click="activeTab = 'list'">
        설문 목록
      </button>
      <button type="button" :class="{ active: activeTab === 'create' }" @click="activeTab = 'create'">
        설문 생성
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

    <p v-if="errorMessage" class="admin-error">{{ errorMessage }}</p>
    <p v-if="isLoading" class="chat-empty">불러오는 중...</p>

    <section v-else-if="activeTab === 'list'" class="admin-section">
      <div class="admin-filter">
        <input v-model="filter.title" type="search" placeholder="설문명 검색" />
        <input v-model="filter.from" type="date" />
        <input v-model="filter.to" type="date" />
        <select v-model="filter.status">
          <option value="">전체 상태</option>
          <option value="DRAFT">작성중</option>
          <option value="ACTIVE">진행중</option>
          <option value="CLOSED">종료</option>
        </select>
        <select v-model="filter.targetMode">
          <option value="">전체 유형</option>
          <option value="ALL_MEMBERS">전체 멤버</option>
          <option value="SELECTED">선택 멤버</option>
          <option value="RANDOM">랜덤 배정</option>
        </select>
        <select v-model="filter.chatRoomId">
          <option value="">전체 채팅방</option>
          <option v-for="room in rooms" :key="room.id" :value="room.id">{{ room.name }}</option>
        </select>
        <button type="button" @click="loadSurveys">조회</button>
        <button type="button" class="secondary" @click="resetFilters">초기화</button>
      </div>

      <table class="admin-table">
        <thead>
          <tr>
            <th>설문명</th>
            <th>채팅방</th>
            <th>상태</th>
            <th>대상</th>
            <th>응답</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="survey in surveys" :key="survey.id">
            <td>{{ survey.title }}</td>
            <td>{{ survey.chatRoomName || '—' }}</td>
            <td>{{ statusLabel(survey.status) }}</td>
            <td>{{ survey.participantCount }}</td>
            <td>{{ survey.completedCount }}</td>
            <td class="admin-actions">
              <button type="button" @click="loadStatistics(survey.id)">통계</button>
              <button type="button" @click="openUploadModal(survey.id)">대상자 업로드</button>
              <template v-if="survey.targetMode === 'RANDOM'">
                <input v-model.number="randomCount" type="number" min="1" class="random-input" />
                <button type="button" @click="assignRandom(survey.id)">랜덤배정</button>
              </template>
              <button
                v-if="survey.status === 'ACTIVE'"
                type="button"
                @click="handleAdminClose(survey.id)"
              >
                종료
              </button>
            </td>
          </tr>
          <tr v-if="surveys.length === 0">
            <td colspan="6" class="empty-cell">설문이 없습니다.</td>
          </tr>
        </tbody>
      </table>
      <PaginationBar
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

    <section v-else-if="activeTab === 'create'" class="admin-section admin-form">
      <label>
        설문 제목
        <input v-model="form.title" type="text" required />
      </label>
      <label>
        안내 문구
        <textarea v-model="form.description" rows="3" />
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
        대상자 모드
        <select v-model="form.targetMode">
          <option value="SELECTED">대상자 선택</option>
          <option value="ALL_MEMBERS">전체 회원</option>
        </select>
      </label>

      <div v-if="form.targetMode === 'SELECTED'" class="user-select-panel">
        <div class="user-select-header">
          <span class="user-select-count">선택됨: {{ selectedUserIds.size }}명 / {{ selectableUsers.length }}명</span>
          <div class="user-select-actions">
            <input v-model.number="form.randomTargetCount" type="number" min="1" class="random-input" />
            <button type="button" @click="randomSelectUsers">랜덤 배정</button>
            <button type="button" class="secondary" @click="selectAllUsers">전체 선택</button>
            <button type="button" class="secondary" @click="deselectAllUsers">전체 해제</button>
          </div>
        </div>
        <div class="user-select-list">
          <label
            v-for="user in selectableUsers"
            :key="user.id"
            class="user-select-item"
            :class="{ selected: selectedUserIds.has(user.id) }"
          >
            <input
              type="checkbox"
              :checked="selectedUserIds.has(user.id)"
              @change="toggleUser(user.id)"
            />
            <span class="user-select-name">{{ user.displayName || user.username }}</span>
            <span v-if="user.displayName" class="user-select-username">({{ user.username }})</span>
          </label>
        </div>
      </div>

      <div v-for="(question, qi) in form.questions" :key="qi" class="question-block">
        <label>
          문항 {{ qi + 1 }}
          <input v-model="question.questionText" type="text" />
        </label>
        <label>
          유형
          <select v-model="question.questionType">
            <option value="SINGLE_CHOICE">단일선택</option>
            <option value="MULTIPLE_CHOICE">복수선택</option>
            <option value="TEXT">주관식</option>
          </select>
        </label>
        <div v-if="question.questionType !== 'TEXT'">
          <div v-for="(option, oi) in question.options" :key="oi">
            <input v-model="option.optionText" type="text" placeholder="보기" />
          </div>
        </div>
      </div>

      <div class="admin-form-actions">
        <button type="button" @click="addQuestion">+ 문항</button>
        <button type="button" class="primary" :disabled="isSaving" @click="createSurvey">
          {{ isSaving ? '생성 중...' : '생성 및 게시' }}
        </button>
      </div>
    </section>

    <section v-else-if="activeTab === 'stats' && statistics" class="admin-section">
      <h2>{{ statistics.title }}</h2>
      <p>참여 {{ statistics.completedParticipants }} / {{ statistics.totalParticipants }}</p>
      <div class="admin-export-actions">
        <button type="button" :disabled="isExporting" @click="handleExportExcel">Excel</button>
        <button type="button" :disabled="isExporting" @click="handleExportPdf">PDF</button>
      </div>

      <nav class="stats-subtabs">
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
        <button
          type="button"
          :class="{ active: statsTab === 'by-room' }"
          @click="statsTab = 'by-room'"
        >
          채팅방별
        </button>
      </nav>

      <div v-if="statsTab === 'by-question'">
        <StatisticsBarChart
          v-if="chartLabels.length > 0"
          :labels="chartLabels"
          :values="chartValues"
        />
        <div v-for="q in statistics.byQuestion" :key="q.questionId" class="stat-card">
          <strong>{{ q.questionNo }}. {{ q.questionText }}</strong>
          <ul>
            <li v-for="opt in q.options" :key="opt.id">{{ opt.optionText }}: {{ opt.selectCount ?? 0 }}</li>
            <li v-for="(ans, i) in q.textAnswers" :key="i">{{ ans }}</li>
          </ul>
        </div>
      </div>

      <div v-else-if="statsTab === 'by-participant'">
        <div v-for="p in pagedParticipants" :key="p.userId" class="stat-card">
          <strong>{{ p.displayName ?? p.username }}</strong>
          <ul>
            <li v-for="a in p.answers" :key="a.questionId">
              {{ a.questionText }}:
              {{ a.textAnswer ?? a.optionTexts.join(', ') }}
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

      <div v-else-if="roomStats">
        <div v-for="row in pagedRoomStats" :key="row.chatRoomId" class="stat-card">
          <strong>{{ row.chatRoomName }}</strong>
          <p>설문 {{ row.surveyCount }}건 · 응답 {{ row.completedCount }}건</p>
        </div>
        <PaginationBar
          v-if="roomStats.rows.length"
          :page="roomFilterPagination.page.value"
          :total-pages="roomFilterPagination.totalPages.value"
          :total-elements="roomFilterPagination.totalElements.value"
          :has-prev="roomFilterPagination.hasPrev.value"
          :has-next="roomFilterPagination.hasNext.value"
          :page-label="roomFilterPagination.pageLabel.value"
          @prev="roomFilterPagination.goPrev()"
          @next="roomFilterPagination.goNext()"
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

<style scoped src="../styles/views/AdminSurveysView.css"></style>
