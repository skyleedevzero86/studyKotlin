<script lang="ts">
  import "../css/Admin.css";
  import * as api from "../../infrastructure/http/api";
  import { statusLabel } from "../ts/Admin";
  import { healthApi } from "../../infrastructure/http/authApi";

  let list = $state<api.AdminUserListResponse | null>(null);
  let search = $state("");
  let page = $state(0);
  let size = 20;
  let message = $state<{ type: "ok" | "error"; text: string } | null>(null);
  let loading = $state(false);
  let historyUserId = $state<number | null>(null);
  let historyData = $state<api.AdminPasswordHistoryResponse | null>(null);
  let emailUserId = $state<number | null>(null);
  let emailValue = $state<string | null>(null);
  let health = $state<{
    status: string;
    service: string;
    timestamp: string;
  } | null>(null);
  let healthLoading = $state(false);
  let healthError = $state<string | null>(null);

  async function loadHealth() {
    healthLoading = true;
    healthError = null;
    try {
      const h = await healthApi.check();
      health = { status: h.status, service: h.service, timestamp: h.timestamp };
    } catch (e) {
      health = null;
      const msg = e instanceof Error ? e.message : "연동 실패";
      healthError = msg === "Failed to fetch" || msg === "Load failed" ? "연동 실패" : msg;
    } finally {
      healthLoading = false;
    }
  }

  $effect(() => {
    if (health === null && !healthLoading && healthError === null) loadHealth();
  });

  async function loadList() {
    loading = true;
    try {
      const data = await api.adminUsersList(page, size, search || null);
      list = data;
    } finally {
      loading = false;
    }
  }

  $effect(() => {
    loadList();
  });

  async function doSearch(e: Event) {
    e.preventDefault();
    page = 0;
    await loadList();
  }

  async function approve(userId: number) {
    message = null;
    const result = await api.adminUsersApprove(userId);
    if ("error" in result) {
      message = { type: "error", text: result.error };
    } else {
      message = { type: "ok", text: "승인되었습니다." };
      await loadList();
    }
  }

  async function suspend(userId: number) {
    message = null;
    const result = await api.adminUsersSuspend(userId);
    if ("error" in result) {
      message = { type: "error", text: result.error };
    } else {
      message = { type: "ok", text: "정지되었습니다." };
      await loadList();
    }
  }

  async function withdraw(userId: number) {
    if (!confirm("해당 회원을 탈퇴 처리하시겠습니까?")) return;
    message = null;
    const result = await api.adminUsersWithdraw(userId);
    if ("error" in result) {
      message = { type: "error", text: result.error };
    } else {
      message = { type: "ok", text: "탈퇴 처리되었습니다." };
      await loadList();
    }
  }

  async function showHistory(userId: number) {
    historyUserId = userId;
    const data = await api.adminUsersPasswordHistory(userId, 0, 20);
    historyData = data;
  }

  function closeHistory() {
    historyUserId = null;
    historyData = null;
  }

  async function showEmail(userId: number) {
    emailUserId = userId;
    const value = await api.adminUsersDecryptedEmail(userId);
    emailValue = value;
  }

  function closeEmail() {
    emailUserId = null;
    emailValue = null;
  }
</script>

<div class="admin-card">
  <h1>관리자 · 회원 관리</h1>

  <section class="admin-health">
    <h2>서버 상태 (Backend Health)</h2>
    {#if healthLoading && !health}
      <p class="health-loading">확인 중…</p>
    {:else if healthError}
      <p class="health-error">{healthError}</p>
      <button type="button" onclick={loadHealth}>다시 확인</button>
    {:else if health}
      <dl class="health-dl">
        <dt>서비스</dt>
        <dd>{health.service}</dd>
        <dt>상태</dt>
        <dd>
          <span class="health-status" class:status-down={health.status !== "UP"}
            >{health.status}</span
          >
        </dd>
        <dt>기준 시각</dt>
        <dd>{health.timestamp}</dd>
      </dl>
      <button type="button" onclick={loadHealth} disabled={healthLoading}
        >{healthLoading ? "확인 중…" : "새로고침"}</button
      >
    {/if}
  </section>

  <form onsubmit={doSearch} class="search-form">
    <input type="text" bind:value={search} placeholder="아이디 검색" />
    <button type="submit">검색</button>
  </form>

  {#if message}
    <p class="message" data-type={message.type}>{message.text}</p>
  {/if}

  {#if loading && !list}
    <p>로딩 중…</p>
  {:else if list}
    <table class="user-table">
      <thead>
        <tr>
          <th>아이디</th>
          <th>이메일</th>
          <th>역할</th>
          <th>상태</th>
          <th>가입일</th>
          <th>관리</th>
        </tr>
      </thead>
      <tbody>
        {#each list.content as u}
          <tr>
            <td>{u.username}</td>
            <td>
              {#if u.emailMasked}
                <button
                  type="button"
                  class="link-btn"
                  onclick={() => showEmail(u.id)}>{u.emailMasked}</button
                >
              {:else}
                -
              {/if}
            </td>
            <td>{u.roles}</td>
            <td>{statusLabel(u.status)}</td>
            <td>{new Date(u.createdAt).toLocaleDateString()}</td>
            <td class="actions">
              {#if u.status !== "WITHDRAWN"}
                {#if u.status === "PENDING"}
                  <button
                    type="button"
                    class="btn-sm"
                    onclick={() => approve(u.id)}>승인</button
                  >
                {/if}
                {#if u.status === "ACTIVE"}
                  <button
                    type="button"
                    class="btn-sm warn"
                    onclick={() => suspend(u.id)}>정지</button
                  >
                {/if}
                {#if u.status === "PENDING" || u.status === "ACTIVE" || u.status === "SUSPENDED"}
                  <button
                    type="button"
                    class="btn-sm danger"
                    onclick={() => withdraw(u.id)}>탈퇴</button
                  >
                {/if}
              {/if}
              <button
                type="button"
                class="btn-sm"
                onclick={() => showHistory(u.id)}>비밀번호 이력</button
              >
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
    <div class="pagination">
      {#if page > 0}
        <button
          type="button"
          onclick={() => {
            page--;
            loadList();
          }}>이전</button
        >
      {/if}
      <span
        >페이지 {list.number + 1} / {list.totalPages || 1} (총 {list.totalElements}명)</span
      >
      {#if list.number < list.totalPages - 1}
        <button
          type="button"
          onclick={() => {
            page++;
            loadList();
          }}>다음</button
        >
      {/if}
    </div>
  {:else if !loading}
    <p>권한이 없거나 로그인이 필요합니다.</p>
  {/if}
</div>

{#if historyUserId !== null}
  <div class="modal" role="dialog">
    <div class="modal-content">
      <h2>비밀번호 변경 이력</h2>
      {#if historyData}
        <p>사용자: {historyData.username}</p>
        <ul>
          {#each historyData.content as h}
            <li>{new Date(h.changedAt).toLocaleString()}</li>
          {/each}
        </ul>
      {:else}
        <p>로딩 중…</p>
      {/if}
      <button type="button" onclick={closeHistory}>닫기</button>
    </div>
  </div>
{/if}

{#if emailUserId !== null}
  <div class="modal" role="dialog">
    <div class="modal-content">
      <h2>이메일 (복호화)</h2>
      <p>{emailValue ?? "없음"}</p>
      <button type="button" onclick={closeEmail}>닫기</button>
    </div>
  </div>
{/if}
