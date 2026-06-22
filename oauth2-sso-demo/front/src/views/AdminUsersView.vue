<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminUsers } from '../composables/useAdminUsers'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { accessToken, logout, isAdmin } = useAuth()
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
  activate,
  unlock,
  withdraw,
  changeRole,
  remove,
  resetPwdFail,
  resetLoginFail,
  addUser,
} = useAdminUsers()

const newUserForm = reactive({
  username: '',
  password: '',
  displayName: '',
  role: 'USER' as 'USER' | 'ADMIN',
  activateImmediately: true,
})

onMounted(async () => {
  if (!accessToken.value || !isAdmin.value) {
    await router.push({ name: 'home' })
    return
  }
  await loadUsers(accessToken.value)
})

const token = () => accessToken.value ?? ''

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
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
  })
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
    await approve(token(), username, role)
  })

const handleChangeRole = (username: string) =>
  run(async () => {
    const role = confirm('ADMIN 권한으로 변경할까요? (취소=USER)') ? 'ADMIN' : 'USER'
    await changeRole(token(), username, role)
  })

const handleDelete = (username: string) =>
  run(async () => {
    if (confirm(`'${username}' 회원을 영구 삭제할까요?`)) {
      await remove(token(), username)
    }
  })

const handleWithdraw = (username: string) =>
  run(async () => {
    if (confirm(`'${username}' 회원을 탈퇴 처리할까요?`)) {
      await withdraw(token(), username)
    }
  })
</script>

<template>
  <main class="admin-page">
    <section class="admin-card">
      <header class="admin-header">
        <div>
          <h1>관리자 · 사용자 목록</h1>
          <p>행 더블클릭으로 민감 정보를 확인하고, 버튼으로 회원을 관리합니다.</p>
        </div>
        <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
      </header>

      <section class="admin-form-section">
        <h2>회원 추가</h2>
        <form class="admin-inline-form" @submit.prevent="handleAddUser">
          <input v-model="newUserForm.username" placeholder="아이디" required minlength="4" />
          <input v-model="newUserForm.password" type="password" placeholder="비밀번호" required minlength="8" />
          <input v-model="newUserForm.displayName" placeholder="표시 이름 (선택)" />
          <select v-model="newUserForm.role">
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
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
              <td>{{ user.revealed ? user.sensitive?.role : '••••••••' }}</td>
              <td>{{ user.revealed ? user.sensitive?.status : '••••••••' }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.createdAt ?? null : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.passwordChangedAt ?? null : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.passwordChangeFailCount ?? 0 : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.loginFailCount ?? 0 : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.lastLoginAt ?? null : null) }}</td>
              <td class="actions-cell" @dblclick.stop>
                <template v-if="user.revealed && user.sensitive">
                  <button v-if="user.sensitive.status === 'PENDING'" type="button" @click="handleApprove(user.username)">승인</button>
                  <button v-if="user.sensitive.status === 'ACTIVE'" type="button" @click="run(() => suspend(token(), user.username))">정지</button>
                  <button v-if="['SUSPENDED','PASSWORD_LOCKED'].includes(user.sensitive.status)" type="button" @click="run(() => activate(token(), user.username))">활성화</button>
                  <button v-if="user.sensitive.status === 'PASSWORD_LOCKED'" type="button" @click="run(() => unlock(token(), user.username))">잠금해제</button>
                  <button type="button" @click="handleChangeRole(user.username)">권한</button>
                  <button type="button" @click="handleWithdraw(user.username)">탈퇴</button>
                  <button type="button" @click="run(() => resetPwdFail(token(), user.username))">변경실패초기화</button>
                  <button type="button" @click="run(() => resetLoginFail(token(), user.username))">로그인실패초기화</button>
                  <button type="button" class="danger" @click="handleDelete(user.username)">삭제</button>
                </template>
                <span v-else class="hint">더블클릭</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <p class="hint">더블클릭: 민감 정보 표시 / 다시 더블클릭: 숨김</p>
    </section>
  </main>
</template>
