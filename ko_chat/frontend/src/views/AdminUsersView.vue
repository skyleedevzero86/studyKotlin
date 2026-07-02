<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PaginationBar from '../components/PaginationBar.vue'
import { useAdminUsers } from '../composables/useAdminUsers'
import { useAuth } from '../composables/useAuth'
import { usePagination } from '../composables/usePagination'
import { formatRole, formatStatus, ROLE_OPTIONS } from '../utils/labels'

const router = useRouter()
const { accessToken, logout, isAdmin } = useAuth()
const pagination = usePagination(20)
const {
  users,
  isLoading,
  errorMessage,
  actionMessage,
  loadUsers,
  toggleReveal,
  displayValue,
  approve,
  suspend,
  restore,
  unlock,
  withdraw,
  changeRole,
  remove,
  resetPwdFail,
  resetLoginFail,
  addUser,
  updateProfile,
} = useAdminUsers()

const newUserForm = reactive({
  username: '',
  password: '',
  displayName: '',
  role: 'USER' as 'USER' | 'ADMIN',
  activateImmediately: true,
})

const editingUser = ref<{ username: string; displayName: string } | null>(null)

const reloadUsers = async () => {
  if (!accessToken.value) return
  const response = await loadUsers(
    accessToken.value,
    pagination.page.value,
    pagination.size.value,
  )
  if (response) pagination.applyPageResponse(response)
}

onMounted(async () => {
  if (!accessToken.value || !isAdmin.value) {
    await router.push({ name: 'home' })
    return
  }
  await reloadUsers()
})

watch(() => pagination.page.value, () => {
  void reloadUsers()
})

const pageArgs = () => [pagination.page.value, pagination.size.value] as const

const token = () => accessToken.value ?? ''

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}

const goProfile = async () => {
  await router.push({ name: 'profile' })
}

const goChat = async () => {
  await router.push({ name: 'home' })
}

const goAdminChatRooms = async () => {
  await router.push({ name: 'admin-chat-rooms' })
}

const goAdminStatistics = async () => {
  await router.push({ name: 'admin-statistics' })
}

const goBack = async () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  await router.push({ name: 'home' })
}

const handleDoubleClick = async (username: string) => {
  await toggleReveal(username)
}

const handleAddUser = async () => {
  if (!token()) return
  await addUser(token(), {
    username: newUserForm.username,
    password: newUserForm.password,
    displayName: newUserForm.displayName.trim() || null,
    role: newUserForm.role,
    activateImmediately: newUserForm.activateImmediately,
  }, ...pageArgs())
  newUserForm.username = ''
  newUserForm.password = ''
  newUserForm.displayName = ''
}

const run = async (fn: () => Promise<void>) => {
  if (!token()) return
  await fn()
}

const handleApprove = (username: string) =>
  run(async () => {
    const role = confirm('관리자 권한으로 승인할까요? (취소=일반 사용자)') ? 'ADMIN' : 'USER'
    await approve(token(), username, role, ...pageArgs())
  })

const handleChangeRole = (username: string) =>
  run(async () => {
    const role = confirm('관리자 권한으로 변경할까요? (취소=일반 사용자)') ? 'ADMIN' : 'USER'
    await changeRole(token(), username, role, ...pageArgs())
  })

const handleDelete = (username: string) =>
  run(async () => {
    if (confirm(`'${username}' 회원을 영구 삭제할까요?`)) {
      await remove(token(), username, ...pageArgs())
    }
  })

const handleRestore = (username: string) =>
  run(async () => {
    if (confirm(`'${username}' 회원을 복구할까요? 정상 이용 상태로 되돌리고 로그인할 수 있습니다.`)) {
      await restore(token(), username, ...pageArgs())
    }
  })

const handleWithdraw = (username: string) =>
  run(async () => {
    if (confirm(`'${username}' 회원을 탈퇴 처리할까요?`)) {
      await withdraw(token(), username, ...pageArgs())
    }
  })

const openEditProfile = (username: string, displayName: string | null) => {
  editingUser.value = { username, displayName: displayName ?? '' }
}

const closeEditProfile = () => {
  editingUser.value = null
}

const saveEditProfile = async () => {
  if (!token() || !editingUser.value) return
  await updateProfile(token(), editingUser.value.username, editingUser.value.displayName.trim() || null, ...pageArgs())
  editingUser.value = null
}

const formatRoleValue = (username: string): string => {
  const target = users.value.find((user) => user.username === username)
  if (!target?.revealed || !target.sensitive) return '••••••••'
  return formatRole(target.sensitive.role)
}

const formatStatusValue = (username: string): string => {
  const target = users.value.find((user) => user.username === username)
  if (!target?.revealed || !target.sensitive) return '••••••••'
  return formatStatus(target.sensitive.status)
}
</script>

<template>
  <main class="admin-page">
    <section class="admin-card">
      <header class="admin-header">
        <div>
          <h1>관리자 · 사용자 목록</h1>
          <p>행 더블클릭으로 민감 정보를 확인하고, 버튼으로 회원을 관리합니다.</p>
        </div>
        <div class="header-actions">
          <button type="button" class="secondary" @click="goBack">이전</button>
          <button type="button" @click="goChat">채팅</button>
          <button type="button" @click="goAdminChatRooms">채팅방 관리</button>
          <button type="button" @click="goAdminStatistics">통계</button>
          <button type="button" @click="goProfile">내 정보</button>
          <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
        </div>
      </header>

      <section class="admin-form-section">
        <h2>회원 추가</h2>
        <form class="admin-inline-form" @submit.prevent="handleAddUser">
          <input v-model="newUserForm.username" placeholder="아이디" required minlength="4" />
          <input v-model="newUserForm.password" type="password" placeholder="비밀번호" required minlength="8" />
          <input v-model="newUserForm.displayName" placeholder="표시 이름 (선택)" />
          <select v-model="newUserForm.role">
            <option v-for="option in ROLE_OPTIONS" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <label class="checkbox-label">
            <input v-model="newUserForm.activateImmediately" type="checkbox" />
            즉시 활성화
          </label>
          <button type="submit">등록</button>
        </form>
      </section>

      <p v-if="actionMessage" class="success" role="status">{{ actionMessage }}</p>
      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
      <p v-if="isLoading" class="hint">불러오는 중...</p>

      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>아이디</th>
              <th>이름</th>
              <th>권한</th>
              <th>상태</th>
              <th>가입일</th>
              <th>비밀번호 변경일</th>
              <th>변경 실패</th>
              <th>로그인 실패</th>
              <th>마지막 로그인</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="user in users"
              :key="user.username"
              :class="{ revealed: user.revealed }"
              @dblclick="handleDoubleClick(user.username)"
            >
              <td class="username">{{ user.username }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.displayName ?? null : null) }}</td>
              <td>{{ formatRoleValue(user.username) }}</td>
              <td>{{ formatStatusValue(user.username) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.createdAt ?? null : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.passwordChangedAt ?? null : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.passwordChangeFailCount ?? 0 : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.loginFailCount ?? 0 : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.lastLoginAt ?? null : null) }}</td>
              <td class="actions-cell" @dblclick.stop>
                <template v-if="user.revealed && user.sensitive">
                  <button type="button" @click="openEditProfile(user.username, user.sensitive.displayName)">정보수정</button>
                  <button v-if="user.sensitive.status === 'PENDING'" type="button" @click="handleApprove(user.username)">승인</button>
                  <button v-if="user.sensitive.status === 'ACTIVE'" type="button" @click="run(() => suspend(token(), user.username, ...pageArgs()))">정지</button>
                  <button v-if="['WITHDRAWN', 'SUSPENDED'].includes(user.sensitive.status)" type="button" @click="handleRestore(user.username)">복구</button>
                  <button v-if="user.sensitive.status === 'PASSWORD_LOCKED'" type="button" @click="run(() => unlock(token(), user.username, ...pageArgs()))">잠금해제</button>
                  <button type="button" @click="handleChangeRole(user.username)">권한</button>
                  <button type="button" @click="handleWithdraw(user.username)">탈퇴</button>
                  <button type="button" @click="run(() => resetPwdFail(token(), user.username, ...pageArgs()))">변경실패초기화</button>
                  <button type="button" @click="run(() => resetLoginFail(token(), user.username, ...pageArgs()))">로그인실패초기화</button>
                  <button type="button" class="danger" @click="handleDelete(user.username)">삭제</button>
                </template>
                <span v-else class="hint">더블클릭</span>
              </td>
            </tr>
          </tbody>
        </table>
        <PaginationBar
          :page="pagination.page.value"
          :total-pages="pagination.totalPages.value"
          :total-elements="pagination.totalElements.value"
          :has-prev="pagination.hasPrev.value"
          :has-next="pagination.hasNext.value"
          :page-label="pagination.pageLabel.value"
          @prev="pagination.goPrev()"
          @next="pagination.goNext()"
        />
      </div>

      <p class="hint">더블클릭: 민감 정보 표시 / 다시 더블클릭: 숨김</p>
    </section>

    <div v-if="editingUser" class="modal-overlay" @click.self="closeEditProfile">
      <section class="modal-card">
        <h2>회원 정보 수정</h2>
        <p class="hint">아이디: {{ editingUser.username }}</p>
        <form class="profile-form" @submit.prevent="saveEditProfile">
          <label>
            표시 이름
            <input v-model="editingUser.displayName" type="text" maxlength="50" />
          </label>
          <div class="modal-actions">
            <button type="button" class="secondary" @click="closeEditProfile">취소</button>
            <button type="submit">저장</button>
          </div>
        </form>
      </section>
    </div>
  </main>
</template>
