<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMessagingOperations, requeueFailedOutbox } from '../api/messagingApi'
import PaginationBar from '../components/PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import { useAuth } from '../composables/useAuth'
import type { MessagingOperationsSnapshot } from '../types/messaging'
import { paginateArray } from '../utils/paginateArray'
import { resolveApiError } from '../utils/resolveApiError'

const LAG_PAGE_SIZE = 15
const lagPagination = usePagination(LAG_PAGE_SIZE)

const router = useRouter()
const { accessToken, logout, isAdmin } = useAuth()

const snapshot = ref<MessagingOperationsSnapshot | null>(null)
const isLoading = ref(false)
const isActing = ref(false)
const errorMessage = ref<string | null>(null)
const actionMessage = ref<string | null>(null)

const lagRows = computed(() => snapshot.value?.consumerLag ?? [])
const pagedLagRows = computed(() =>
  paginateArray(lagRows.value, lagPagination.page.value, lagPagination.size.value),
)

const syncLagPagination = () => {
  const total = lagRows.value.length
  lagPagination.totalElements.value = total
  lagPagination.totalPages.value = Math.max(1, Math.ceil(total / lagPagination.size.value))
  if (lagPagination.page.value > 0 && lagPagination.page.value >= lagPagination.totalPages.value) {
    lagPagination.resetPage()
  }
}

const goLagPrev = () => {
  lagPagination.goPrev()
}

const goLagNext = () => {
  lagPagination.goNext()
}

const loadSnapshot = async () => {
  if (!accessToken.value) return
  isLoading.value = true
  errorMessage.value = null
  try {
    snapshot.value = await getMessagingOperations(accessToken.value)
    syncLagPagination()
  } catch (error) {
    errorMessage.value = resolveApiError(error, '메시징 운영 정보를 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

const handleRequeueFailed = async () => {
  if (!accessToken.value) return
  isActing.value = true
  actionMessage.value = null
  try {
    const result = await requeueFailedOutbox(accessToken.value)
    actionMessage.value = `FAILED Outbox ${result.requeued}건을 재큐잉했습니다.`
    await loadSnapshot()
  } catch (error) {
    errorMessage.value = resolveApiError(error, 'Outbox 재큐잉에 실패했습니다.')
  } finally {
    isActing.value = false
  }
}

onMounted(async () => {
  if (!accessToken.value || !isAdmin.value) {
    await router.push({ name: 'home' })
    return
  }
  await loadSnapshot()
})

const goBack = async () => router.back()
const goChat = async () => router.push({ name: 'home' })
const goAdminUsers = async () => router.push({ name: 'admin-users' })
const goAdminChatRooms = async () => router.push({ name: 'admin-chat-rooms' })
const goAdminStatistics = async () => router.push({ name: 'admin-statistics' })
const goProfile = async () => router.push({ name: 'profile' })

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <main class="admin-page">
    <section class="admin-card messaging-card">
      <header class="admin-header">
        <div>
          <h1>관리자 · 메시징 운영</h1>
          <p>Outbox, processed_events, DLQ, Kafka consumer lag를 모니터링합니다.</p>
        </div>
        <div class="header-actions">
          <button type="button" class="secondary" @click="goBack">이전</button>
          <button type="button" @click="goChat">채팅</button>
          <button type="button" @click="goAdminUsers">사용자 관리</button>
          <button type="button" @click="goAdminChatRooms">채팅방 관리</button>
          <button type="button" @click="goAdminStatistics">통계</button>
          <button type="button" @click="goProfile">내 정보</button>
          <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
        </div>
      </header>

      <div class="messaging-toolbar">
        <button type="button" :disabled="isLoading" @click="loadSnapshot">새로고침</button>
        <button type="button" :disabled="isActing || isLoading" @click="handleRequeueFailed">
          FAILED Outbox 재큐잉
        </button>
      </div>

      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
      <p v-if="actionMessage" class="hint">{{ actionMessage }}</p>
      <p v-if="isLoading" class="hint">불러오는 중...</p>

      <template v-else-if="snapshot">
        <section class="metrics-grid">
          <article class="metric-card">
            <h2>Outbox</h2>
            <ul>
              <li>PENDING: {{ snapshot.outbox.pending }}</li>
              <li>PUBLISHED: {{ snapshot.outbox.published }}</li>
              <li>FAILED: {{ snapshot.outbox.failed }}</li>
            </ul>
          </article>

          <article class="metric-card">
            <h2>processed_events</h2>
            <p>총 {{ snapshot.processedEvents.total }}건</p>
            <ul>
              <li v-for="(count, name) in snapshot.processedEvents.byConsumer" :key="name">
                {{ name }}: {{ count }}
              </li>
            </ul>
          </article>

          <article class="metric-card">
            <h2>DLQ</h2>
            <ul>
              <li>OPEN: {{ snapshot.dlq.open }}</li>
              <li>REPLAYED: {{ snapshot.dlq.replayed }}</li>
            </ul>
          </article>

          <article class="metric-card" :class="{ unhealthy: !snapshot.lagHealthy }">
            <h2>Kafka Lag</h2>
            <p>{{ snapshot.lagHealthy ? '정상' : '지연 경고' }}</p>
            <p class="hint">확인: {{ snapshot.checkedAt }}</p>
          </article>
        </section>

        <section v-if="snapshot.consumerLag.length" class="table-wrap">
          <h2>Consumer Lag 상세</h2>
          <table>
            <thead>
              <tr>
                <th>Consumer Group</th>
                <th>Topic</th>
                <th>Partition</th>
                <th>Current</th>
                <th>End</th>
                <th>Lag</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in pagedLagRows" :key="`${row.consumerGroup}-${row.partition}`">
                <td>{{ row.consumerGroup }}</td>
                <td>{{ row.topic }}</td>
                <td>{{ row.partition }}</td>
                <td>{{ row.currentOffset }}</td>
                <td>{{ row.endOffset }}</td>
                <td :class="{ 'lag-high': row.lag >= 100 }">{{ row.lag }}</td>
              </tr>
            </tbody>
          </table>
          <PaginationBar
            v-if="lagRows.length > LAG_PAGE_SIZE"
            :page="lagPagination.page.value"
            :total-pages="lagPagination.totalPages.value"
            :total-elements="lagPagination.totalElements.value"
            :has-prev="lagPagination.hasPrev.value"
            :has-next="lagPagination.hasNext.value"
            :page-label="lagPagination.pageLabel.value"
            @prev="goLagPrev()"
            @next="goLagNext()"
          />
        </section>
      </template>
    </section>
  </main>
</template>

<style scoped src="../styles/views/AdminMessagingView.css"></style>
