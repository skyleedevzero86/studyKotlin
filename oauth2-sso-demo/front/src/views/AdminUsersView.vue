<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminUsers } from '../composables/useAdminUsers'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { accessToken, logout, isAdmin } = useAuth()
const { users, isLoading, errorMessage, loadUsers, toggleReveal, displayValue } = useAdminUsers()

onMounted(async () => {
  if (!accessToken.value || !isAdmin.value) {
    await router.push({ name: 'home' })
    return
  }
  await loadUsers(accessToken.value)
})

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}

const handleDoubleClick = async (username: string) => {
  await toggleReveal(username)
}
</script>

<template>
  <main class="admin-page">
    <section class="admin-card">
      <header class="admin-header">
        <div>
          <h1>관리자 · 사용자 목록</h1>
          <p>민감 정보는 AES-256으로 암호화되어 있습니다. 행을 더블클릭하면 복호화됩니다.</p>
        </div>
        <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
      </header>

      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
      <p v-if="isLoading" class="hint">불러오는 중...</p>

      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>아이디</th>
              <th>권한</th>
              <th>상태</th>
              <th>가입일</th>
              <th>비밀번호 변경일</th>
              <th>변경 실패</th>
              <th>마지막 로그인</th>
              <th>만료</th>
              <th>남은 일수</th>
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
              <td>{{ user.revealed ? user.sensitive?.role : '••••••••' }}</td>
              <td>{{ user.revealed ? user.sensitive?.status : '••••••••' }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.createdAt ?? null : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.passwordChangedAt ?? null : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.passwordChangeFailCount ?? 0 : null) }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.lastLoginAt ?? null : null) }}</td>
              <td>{{ user.revealed ? (user.sensitive?.passwordExpired ? 'Y' : 'N') : '••••••••' }}</td>
              <td>{{ displayValue(user.username, user.revealed ? user.sensitive?.daysUntilPasswordChange ?? 0 : null) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <p class="hint">더블클릭: 민감 정보 표시 / 다시 더블클릭: 숨김</p>
    </section>
  </main>
</template>
