<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  exportStatisticsExcel,
  exportStatisticsPdf,
  getHourlyStatistics,
  getMessageTypeStatistics,
  getRoomTypeStatistics,
} from '../api/statisticsApi'
import StatisticsBarChart, { type ChartDataset } from '../components/StatisticsBarChart.vue'
import { useAuth } from '../composables/useAuth'
import type {
  MessageTypeYearStatisticsResponse,
  RoomTypeDailyStatisticsResponse,
  StatisticsFilterState,
  StatisticsPeriodResponse,
  StatisticsTab,
} from '../types/statistics'
import { resolveApiError } from '../utils/resolveApiError'

const router = useRouter()
const { accessToken, logout, isAdmin } = useAuth()

const activeTab = ref<StatisticsTab>('hourly')
const isLoading = ref(false)
const isExporting = ref(false)
const errorMessage = ref<string | null>(null)

const hourlyData = ref<StatisticsPeriodResponse | null>(null)
const messageTypeData = ref<MessageTypeYearStatisticsResponse | null>(null)
const roomTypeData = ref<RoomTypeDailyStatisticsResponse | null>(null)

const today = new Date()
const defaultFrom = `${today.getFullYear()}-01-01`
const defaultTo = today.toISOString().slice(0, 10)

const filter = reactive<StatisticsFilterState>({
  from: defaultFrom,
  to: defaultTo,
  roomType: '',
  messageType: '',
})

const tabTitle = computed(() => {
  switch (activeTab.value) {
    case 'hourly':
      return '시간대별 통계'
    case 'message-types':
      return '메시지 유형별 통계'
    default:
      return '채팅방 유형별 통계'
  }
})

const chartLabels = computed(() => {
  if (activeTab.value === 'hourly' && hourlyData.value) {
    return hourlyData.value.rows.map((row) => row.label)
  }
  if (activeTab.value === 'message-types' && messageTypeData.value) {
    return messageTypeData.value.rows.map((row) => `${row.year}년`)
  }
  if (activeTab.value === 'room-types' && roomTypeData.value) {
    return roomTypeData.value.rows.map((row) => row.date)
  }
  return []
})

const chartValues = computed(() => {
  if (activeTab.value === 'hourly' && hourlyData.value) {
    return hourlyData.value.rows.map((row) => row.count)
  }
  if (activeTab.value === 'message-types' && messageTypeData.value) {
    return messageTypeData.value.rows.map((row) => row.total)
  }
  if (activeTab.value === 'room-types' && roomTypeData.value) {
    return roomTypeData.value.rows.map((row) => row.total)
  }
  return []
})

const chartDatasets = computed((): ChartDataset[] => {
  if (activeTab.value === 'message-types' && messageTypeData.value) {
    return messageTypeData.value.typeLabels.map((type) => ({
      label: messageTypeLabel(type),
      values: messageTypeData.value!.rows.map((row) => row.types[type]?.count ?? 0),
    }))
  }
  if (activeTab.value === 'room-types' && roomTypeData.value) {
    return roomTypeData.value.typeLabels.map((type) => ({
      label: roomTypeLabel(type),
      values: roomTypeData.value!.rows.map((row) => row.types[type]?.count ?? 0),
    }))
  }
  return []
})

const chartStacked = computed(
  () => activeTab.value === 'message-types' || activeTab.value === 'room-types',
)

const chartKey = computed(
  () => `${activeTab.value}-${filter.from}-${filter.to}-${filter.roomType}-${filter.messageType}`,
)

const hasChartData = computed(() => chartLabels.value.length > 0)

const resolveError = (error: unknown, fallback: string): string =>
  resolveApiError(error, fallback)

const messageTypeLabel = (type: string): string => {
  switch (type) {
    case 'TEXT':
      return '텍스트'
    case 'IMAGE':
      return '이미지'
    case 'FILE':
      return '파일'
    case 'LINK':
      return '링크'
    case 'SYSTEM':
      return '시스템'
    default:
      return type
  }
}

const roomTypeLabel = (type: string): string => {
  switch (type) {
    case 'DIRECT':
      return '1:1'
    case 'GROUP':
      return '그룹'
    case 'CHANNEL':
      return '채널'
    default:
      return type
  }
}

const loadReport = async () => {
  if (!accessToken.value) return
  isLoading.value = true
  errorMessage.value = null
  try {
    if (activeTab.value === 'hourly') {
      hourlyData.value = await getHourlyStatistics(accessToken.value, filter)
    } else if (activeTab.value === 'message-types') {
      messageTypeData.value = await getMessageTypeStatistics(accessToken.value, filter)
    } else {
      roomTypeData.value = await getRoomTypeStatistics(accessToken.value, filter)
    }
  } catch (error) {
    errorMessage.value = resolveError(error, '통계를 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

const resetFilter = () => {
  filter.from = defaultFrom
  filter.to = defaultTo
  filter.roomType = ''
  filter.messageType = ''
}

const switchTab = async (tab: StatisticsTab) => {
  activeTab.value = tab
  await loadReport()
}

const handleExportExcel = async () => {
  if (!accessToken.value) return
  isExporting.value = true
  try {
    await exportStatisticsExcel(accessToken.value, filter, activeTab.value)
  } catch (error) {
    errorMessage.value = resolveError(error, '엑셀 다운로드에 실패했습니다.')
  } finally {
    isExporting.value = false
  }
}

const handleExportPdf = async () => {
  if (!accessToken.value) return
  isExporting.value = true
  try {
    await exportStatisticsPdf(accessToken.value, filter, activeTab.value)
  } catch (error) {
    errorMessage.value = resolveError(error, 'PDF 다운로드에 실패했습니다.')
  } finally {
    isExporting.value = false
  }
}

onMounted(async () => {
  if (!accessToken.value || !isAdmin.value) {
    await router.push({ name: 'home' })
    return
  }
  await loadReport()
})

const goBack = async () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  await router.push({ name: 'home' })
}

const goChat = async () => router.push({ name: 'home' })
const goProfile = async () => router.push({ name: 'profile' })
const goAdminUsers = async () => router.push({ name: 'admin-users' })
const goAdminChatRooms = async () => router.push({ name: 'admin-chat-rooms' })

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <main class="admin-page stats-page">
    <section class="admin-card stats-card">
      <header class="admin-header">
        <div>
          <h1>관리자 · 통계</h1>
          <p>채팅 메시지 데이터를 기준으로 시간대·유형별 현황을 조회합니다.</p>
        </div>
        <div class="header-actions">
          <button type="button" class="secondary" @click="goBack">이전</button>
          <button type="button" @click="goChat">채팅</button>
          <button type="button" @click="goAdminUsers">사용자 관리</button>
          <button type="button" @click="goAdminChatRooms">채팅방 관리</button>
          <button type="button" @click="goProfile">내 정보</button>
          <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
        </div>
      </header>

      <nav class="stats-tabs">
        <button
          type="button"
          :class="{ active: activeTab === 'hourly' }"
          @click="switchTab('hourly')"
        >
          시간대별 통계
        </button>
        <button
          type="button"
          :class="{ active: activeTab === 'message-types' }"
          @click="switchTab('message-types')"
        >
          메시지 유형별 통계
        </button>
        <button
          type="button"
          :class="{ active: activeTab === 'room-types' }"
          @click="switchTab('room-types')"
        >
          채팅방 유형별 통계
        </button>
      </nav>

      <section class="stats-filter-bar">
        <h2>{{ tabTitle }}</h2>
        <form class="stats-filter-form" @submit.prevent="loadReport">
          <label>
            검색기간
            <div class="date-range">
              <input v-model="filter.from" type="date" required />
              <span>~</span>
              <input v-model="filter.to" type="date" required />
            </div>
          </label>
          <label v-if="activeTab === 'hourly' || activeTab === 'message-types'">
            채팅방 유형
            <select v-model="filter.roomType">
              <option value="">전체</option>
              <option value="DIRECT">1:1</option>
              <option value="GROUP">그룹</option>
              <option value="CHANNEL">채널</option>
            </select>
          </label>
          <label v-if="activeTab === 'hourly' || activeTab === 'room-types'">
            메시지 유형
            <select v-model="filter.messageType">
              <option value="">전체</option>
              <option value="TEXT">텍스트</option>
              <option value="IMAGE">이미지</option>
              <option value="FILE">파일</option>
              <option value="LINK">링크</option>
              <option value="SYSTEM">시스템</option>
            </select>
          </label>
          <div class="stats-filter-actions">
            <button type="submit" :disabled="isLoading">조회</button>
            <button type="button" class="secondary" @click="resetFilter">초기화</button>
          </div>
        </form>
      </section>

      <section class="stats-toolbar">
        <button type="button" :disabled="isExporting || isLoading" @click="handleExportExcel">
          엑셀 다운로드
        </button>
        <button type="button" class="secondary" :disabled="isExporting || isLoading" @click="handleExportPdf">
          PDF 다운로드
        </button>
      </section>

      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>

      <section class="stats-report">
        <div class="stats-chart-section">
          <h3 class="stats-chart-heading">{{ tabTitle }} 차트</h3>
          <p v-if="isLoading" class="hint stats-chart-loading">차트 불러오는 중...</p>
          <StatisticsBarChart
            v-else-if="hasChartData"
            :key="chartKey"
            :labels="chartLabels"
            :values="chartDatasets.length ? [] : chartValues"
            :datasets="chartDatasets"
            :title="tabTitle"
            :stacked="chartStacked"
          />
          <div v-else class="stats-chart-placeholder">
            <p class="hint">조회 버튼을 눌러 통계를 불러오면 차트가 표시됩니다.</p>
          </div>
        </div>

        <div class="stats-report-paper" :class="{ 'is-loading': isLoading }">
          <p v-if="isLoading" class="hint stats-table-loading">표 불러오는 중...</p>
          <template v-else>
            <header class="stats-report-header">
              <div>
                <h3 v-if="activeTab === 'hourly' && hourlyData">{{ hourlyData.title }}</h3>
                <h3 v-else-if="activeTab === 'message-types' && messageTypeData">{{ messageTypeData.title }}</h3>
                <h3 v-else-if="activeTab === 'room-types' && roomTypeData">{{ roomTypeData.title }}</h3>
                <p class="hint">검색기간: {{ filter.from }} ~ {{ filter.to }}</p>
              </div>
            </header>

            <div v-if="activeTab === 'hourly' && hourlyData" class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>시간대</th>
                  <th>메시지 건수</th>
                  <th>비율</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in hourlyData.rows" :key="row.label">
                  <td>{{ row.label }}</td>
                  <td>{{ row.count }}</td>
                  <td>{{ row.ratio }}%</td>
                </tr>
                <tr class="total-row">
                  <td>계</td>
                  <td>{{ hourlyData.total }}</td>
                  <td>100%</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="activeTab === 'message-types' && messageTypeData" class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>구분</th>
                  <th v-for="type in messageTypeData.typeLabels" :key="type" colspan="2">
                    {{ messageTypeLabel(type) }}
                  </th>
                  <th>합계</th>
                </tr>
                <tr>
                  <th></th>
                  <template v-for="type in messageTypeData.typeLabels" :key="`${type}-sub`">
                    <th>건수</th>
                    <th>비율</th>
                  </template>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in messageTypeData.rows" :key="row.year">
                  <td>{{ row.year }}년</td>
                  <template v-for="type in messageTypeData.typeLabels" :key="`${row.year}-${type}`">
                    <td>{{ row.types[type]?.count ?? 0 }}</td>
                    <td>{{ row.types[type]?.ratio ?? 0 }}%</td>
                  </template>
                  <td>{{ row.total }}</td>
                </tr>
                <tr class="total-row">
                  <td>계</td>
                  <template v-for="type in messageTypeData.typeLabels" :key="`total-${type}`">
                    <td>{{ messageTypeData.totals[type]?.count ?? 0 }}</td>
                    <td>{{ messageTypeData.totals[type]?.ratio ?? 0 }}%</td>
                  </template>
                  <td>{{ messageTypeData.grandTotal }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="activeTab === 'room-types' && roomTypeData" class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>일자</th>
                  <th v-for="type in roomTypeData.typeLabels" :key="type" colspan="2">
                    {{ roomTypeLabel(type) }}
                  </th>
                  <th>합계</th>
                </tr>
                <tr>
                  <th></th>
                  <template v-for="type in roomTypeData.typeLabels" :key="`${type}-sub`">
                    <th>건수</th>
                    <th>비율</th>
                  </template>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in roomTypeData.rows" :key="row.date">
                  <td>{{ row.date }}</td>
                  <template v-for="type in roomTypeData.typeLabels" :key="`${row.date}-${type}`">
                    <td>{{ row.types[type]?.count ?? 0 }}</td>
                    <td>{{ row.types[type]?.ratio ?? 0 }}%</td>
                  </template>
                  <td>{{ row.total }}</td>
                </tr>
                <tr class="total-row">
                  <td>계</td>
                  <template v-for="type in roomTypeData.typeLabels" :key="`total-${type}`">
                    <td>{{ roomTypeData.totals[type]?.count ?? 0 }}</td>
                    <td>{{ roomTypeData.totals[type]?.ratio ?? 0 }}%</td>
                  </template>
                  <td>{{ roomTypeData.grandTotal }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          </template>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped src="../styles/views/AdminStatisticsView.css"></style>
