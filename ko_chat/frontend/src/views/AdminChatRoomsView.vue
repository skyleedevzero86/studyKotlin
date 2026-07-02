<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  adminKickChatRoomMember,
  getAdminChatRoomMembers,
  getAdminChatRooms,
} from '../api/chatApi'
import PaginationBar from '../components/PaginationBar.vue'
import { useAuth } from '../composables/useAuth'
import { usePagination } from '../composables/usePagination'
import type { ChatRoom, ChatRoomMember } from '../types/chat'
import { resolveApiError } from '../utils/resolveApiError'

const router = useRouter()
const { logout, isAdmin, getValidAccessToken } = useAuth()
const roomPagination = usePagination(20)
const memberPagination = usePagination(10)

const rooms = ref<ChatRoom[]>([])
const isLoading = ref(false)
const errorMessage = ref<string | null>(null)
const actionMessage = ref<string | null>(null)
const expandedRoomId = ref<number | null>(null)
const membersByRoom = ref<Record<number, ChatRoomMember[]>>({})
const membersLoading = ref(false)
const kickingUserId = ref<number | null>(null)

const selectedRoom = computed(() =>
  rooms.value.find((room) => room.id === expandedRoomId.value) ?? null,
)

const selectedMembers = computed(() => {
  const roomId = expandedRoomId.value
  if (roomId === null) return []
  return membersByRoom.value[roomId] ?? []
})

const resolveError = (error: unknown, fallback: string): string =>
  resolveApiError(error, fallback)

const roomTypeLabel = (type: ChatRoom['type']): string => {
  switch (type) {
    case 'DIRECT':
      return '1:1'
    case 'CHANNEL':
      return '채널'
    default:
      return '그룹'
  }
}

const memberLabel = (member: ChatRoomMember): string =>
  member.user.displayName ?? member.user.username

const loadRooms = async () => {
  const token = getValidAccessToken()
  if (!token) {
    await router.push({ name: 'login', query: { reason: 'session-expired' } })
    return
  }
  isLoading.value = true
  errorMessage.value = null
  try {
    const page = await getAdminChatRooms(
      token,
      roomPagination.page.value,
      roomPagination.size.value,
    )
    rooms.value = page.content
    roomPagination.applyPageResponse(page)
  } catch (error) {
    errorMessage.value = resolveError(error, '채팅방 목록을 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

const loadMembers = async (roomId: number) => {
  const token = getValidAccessToken()
  if (!token) return
  membersLoading.value = true
  errorMessage.value = null
  try {
    const page = await getAdminChatRoomMembers(
      token,
      roomId,
      memberPagination.page.value,
      memberPagination.size.value,
    )
    membersByRoom.value[roomId] = page.content
    memberPagination.applyPageResponse(page)
  } catch (error) {
    errorMessage.value = resolveError(error, '멤버 목록을 불러오지 못했습니다.')
  } finally {
    membersLoading.value = false
  }
}

const openMembers = async (roomId: number) => {
  expandedRoomId.value = roomId
  memberPagination.resetPage()
  await loadMembers(roomId)
}

const closeMembers = () => {
  expandedRoomId.value = null
}

const handleKick = async (roomId: number, targetUserId: number, targetName: string) => {
  const token = getValidAccessToken()
  if (!token) return
  if (!confirm(`'${targetName}' 님을 채팅방에서 강제 퇴장시킬까요?`)) return

  kickingUserId.value = targetUserId
  actionMessage.value = null
  errorMessage.value = null
  try {
    await adminKickChatRoomMember(token, roomId, targetUserId)
    actionMessage.value = `'${targetName}' 님을 퇴장시켰습니다.`
    await loadMembers(roomId)
    await loadRooms()
  } catch (error) {
    errorMessage.value = resolveError(error, '강제 퇴장에 실패했습니다.')
  } finally {
    kickingUserId.value = null
  }
}

onMounted(async () => {
  const token = getValidAccessToken()
  if (!token || !isAdmin.value) {
    await router.push({ name: token ? 'home' : 'login', query: token ? undefined : { reason: 'session-expired' } })
    return
  }
  await loadRooms()
})

watch(() => roomPagination.page.value, () => {
  void loadRooms()
})

watch(() => memberPagination.page.value, () => {
  if (expandedRoomId.value !== null) {
    void loadMembers(expandedRoomId.value)
  }
})

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}

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
const goAdminStatistics = async () => router.push({ name: 'admin-statistics' })
</script>

<template>
  <main class="admin-page">
    <section class="admin-card">
      <header class="admin-header">
        <div>
          <h1>관리자 · 채팅방 목록</h1>
          <p>현재 활성화된 채팅방과 참여 중인 멤버를 확인하고 강제 퇴장시킬 수 있습니다.</p>
        </div>
        <div class="header-actions">
          <button type="button" class="secondary" @click="goBack">이전</button>
          <button type="button" @click="goChat">채팅</button>
          <button type="button" @click="goAdminUsers">사용자 관리</button>
          <button type="button" @click="goAdminStatistics">통계</button>
          <button type="button" @click="goProfile">내 정보</button>
          <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
        </div>
      </header>

      <p v-if="actionMessage" class="success" role="status">{{ actionMessage }}</p>
      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
      <p v-if="isLoading" class="hint">불러오는 중...</p>

      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>이름</th>
              <th>유형</th>
              <th>비공개</th>
              <th>인원</th>
              <th>개설자</th>
              <th>생성일</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="room in rooms" :key="room.id">
              <td>{{ room.id }}</td>
              <td>{{ room.name }}</td>
              <td>{{ roomTypeLabel(room.type) }}</td>
              <td>{{ room.isPrivate ? '예' : '아니오' }}</td>
              <td>{{ room.memberCount }} / {{ room.maxMembers }}</td>
              <td>{{ room.createdBy.displayName ?? room.createdBy.username }}</td>
              <td>{{ room.createdAt }}</td>
              <td class="actions-cell" @click.stop>
                <button type="button" @click="openMembers(room.id)">멤버 보기</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-if="!rooms.length" class="hint">활성 채팅방이 없습니다.</p>
        <PaginationBar
          :page="roomPagination.page.value"
          :total-pages="roomPagination.totalPages.value"
          :total-elements="roomPagination.totalElements.value"
          :has-prev="roomPagination.hasPrev.value"
          :has-next="roomPagination.hasNext.value"
          :page-label="roomPagination.pageLabel.value"
          @prev="roomPagination.goPrev()"
          @next="roomPagination.goNext()"
        />
      </div>
    </section>

    <div v-if="selectedRoom" class="modal-overlay" @click.self="closeMembers">
      <section class="modal-card admin-members-modal">
        <h2>{{ selectedRoom.name }} · 멤버</h2>
        <p class="hint">채팅방 ID {{ selectedRoom.id }} · {{ selectedRoom.memberCount }}명 참여 중</p>

        <p v-if="membersLoading" class="hint">멤버 불러오는 중...</p>
        <div v-else-if="selectedMembers.length" class="member-list">
          <div v-for="member in selectedMembers" :key="member.id" class="member-item">
            <span>
              {{ memberLabel(member) }}
              ({{ member.user.username }})
              · {{ member.role }}
              · {{ member.isActive ? '참여 중' : '퇴장' }}
            </span>
            <button
              v-if="member.isActive"
              type="button"
              class="danger"
              :disabled="kickingUserId === member.user.id"
              @click="handleKick(selectedRoom.id, member.user.id, memberLabel(member))"
            >
              강제 퇴장
            </button>
          </div>
        </div>
        <p v-else class="hint">참여 중인 멤버가 없습니다.</p>

        <PaginationBar
          v-if="selectedMembers.length || memberPagination.totalElements.value > 0"
          :page="memberPagination.page.value"
          :total-pages="memberPagination.totalPages.value"
          :total-elements="memberPagination.totalElements.value"
          :has-prev="memberPagination.hasPrev.value"
          :has-next="memberPagination.hasNext.value"
          :page-label="memberPagination.pageLabel.value"
          @prev="memberPagination.goPrev()"
          @next="memberPagination.goNext()"
        />

        <div class="modal-actions">
          <button type="button" class="secondary" @click="closeMembers">닫기</button>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped src="../styles/views/AdminChatRoomsView.css"></style>
