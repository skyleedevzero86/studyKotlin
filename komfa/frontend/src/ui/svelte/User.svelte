<script lang="ts">
  import { onMount } from "svelte";
  import "../css/User.css";
  import * as api from "../../infrastructure/http/api";
  import { statusLabel } from "../ts/Admin";
  import { healthApi } from "../../infrastructure/http/authApi";

  let {
    profile = null,
    isAdmin = false,
  }: { profile?: api.MeResponse | null; isAdmin?: boolean } = $props();

  let data = $state<{ message: string; username: string } | null>(null);
  let list = $state<api.AdminUserListResponse | null>(null);
  let listLoading = $state(false);
  let listError = $state(false);
  let listErrorStatus = $state<number | null>(null);
  let page = $state(0);
  let size = 20;
  let search = $state("");
  let message = $state<{ type: "ok" | "error"; text: string } | null>(null);
  let emailUserId = $state<number | null>(null);
  let emailValue = $state<string | null>(null);
  let historyUserId = $state<number | null>(null);
  let historyData = $state<api.AdminPasswordHistoryResponse | null>(null);
  let historyPage = $state(0);
  const historySize = 10;
  let health = $state<{ status: string; service: string; timestamp: string } | null>(null);
  let healthLoading = $state(false);
  let healthError = $state<string | null>(null);

  $effect(() => {
    if (profile) {
      api.userArea().then((d) => {
        data = d;
      });
    } else {
      data = null;
    }
  });

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
    if (isAdmin && health === null && !healthLoading && healthError === null) loadHealth();
  });

  async function loadList() {
    listLoading = true;
    listError = false;
    listErrorStatus = null;
    try {
      const res = await api.adminUsersList(page, size, search.trim() || null);
      if (res.ok) {
        list = res.data;
      } else {
        list = null;
        listError = true;
        listErrorStatus = res.status;
      }
    } catch {
      list = null;
      listError = true;
      listErrorStatus = null;
    } finally {
      listLoading = false;
    }
  }

  $effect(() => {
    if (profile && isAdmin) loadList();
    else list = null;
  });

  onMount(() => {
    if (profile && isAdmin && !list && !listLoading) loadList();
  });

  function doSearch(e: Event) {
    e.preventDefault();
    page = 0;
    loadList();
  }

  async function showDecryptedEmail(userId: number) {
    emailUserId = userId;
    emailValue = null;
    const value = await api.adminUsersDecryptedEmail(userId);
    emailValue = value;
  }

  function closeEmail() {
    emailUserId = null;
    emailValue = null;
  }

  async function showHistory(userId: number) {
    historyUserId = userId;
    historyPage = 0;
    historyData = null;
    const data = await api.adminUsersPasswordHistory(userId, 0, historySize);
    historyData = data;
  }

  async function loadHistoryPage(nextPage: number) {
    if (historyUserId === null) return;
    historyPage = nextPage;
    const data = await api.adminUsersPasswordHistory(historyUserId, nextPage, historySize);
    historyData = data;
  }

  function closeHistory() {
    historyUserId = null;
    historyData = null;
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

  async function changeRole(userId: number, newRole: "ROLE_USER" | "ROLE_ADMIN") {
    message = null;
    const result = await api.adminUsersUpdateRole(userId, newRole);
    if ("error" in result) {
      message = { type: "error", text: result.error };
    } else {
      message = { type: "ok", text: "권한이 변경되었습니다." };
      await loadList();
    }
  }

  const loggedIn = $derived(!!profile);
</script>

<div class="user-card">
  <h1>회원 전용</h1>
  {#if loggedIn}
    <p>{data?.message ?? "사용자 영역"}</p>
    <p class="username">
      안녕하세요, <strong>{profile?.username ?? ""}</strong>님.
    </p>

    {#if isAdmin && (list !== null || listLoading || listError)}
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
              <span class="health-status" class:status-down={health.status !== "UP"}>{health.status}</span>
            </dd>
            <dt>기준 시각</dt>
            <dd>{health.timestamp}</dd>
          </dl>
          <button type="button" onclick={loadHealth} disabled={healthLoading}>
            {healthLoading ? "확인 중…" : "새로고침"}
          </button>
        {/if}
      </section>

      <section class="member-list">
        <h2>가입 회원 목록</h2>
        <form onsubmit={doSearch} class="search-form">
          <input type="text" bind:value={search} placeholder="아이디 검색" />
          <button type="submit">검색</button>
        </form>

        {#if message}
          <p class="message" data-type={message.type}>{message.text}</p>
        {/if}

        {#if listLoading && !list}
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
                        title="클릭 시 복호화된 이메일 표시"
                        onclick={() => showDecryptedEmail(u.id)}>{u.emailMasked}</button>
                    {:else}
                      -
                    {/if}
                  </td>
                  <td class="roles-cell">
                    <span class="role-text">{u.roles.includes("ROLE_ADMIN") ? "관리자" : "일반회원"}</span>
                    {#if u.status !== "WITHDRAWN"}
                      {#if u.roles.includes("ROLE_ADMIN")}
                        <button type="button" class="btn-sm" onclick={() => changeRole(u.id, "ROLE_USER")}>일반회원으로</button>
                      {:else}
                        <button type="button" class="btn-sm" onclick={() => changeRole(u.id, "ROLE_ADMIN")}>관리자로</button>
                      {/if}
                    {/if}
                  </td>
                  <td>{statusLabel(u.status)}</td>
                  <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                  <td class="actions">
                    {#if u.status !== "WITHDRAWN"}
                      {#if u.status === "PENDING"}
                        <button type="button" class="btn-sm" onclick={() => approve(u.id)}>승인</button>
                      {/if}
                      {#if u.status === "ACTIVE"}
                        <button type="button" class="btn-sm warn" onclick={() => suspend(u.id)}>정지</button>
                      {/if}
                      {#if u.status === "PENDING" || u.status === "ACTIVE" || u.status === "SUSPENDED"}
                        <button type="button" class="btn-sm danger" onclick={() => withdraw(u.id)}>탈퇴</button>
                      {/if}
                    {/if}
                    <button type="button" class="btn-sm" onclick={() => showHistory(u.id)}>비밀번호 이력</button>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
          <div class="pagination">
            {#if page > 0}
              <button type="button" onclick={() => { page--; loadList(); }}>이전</button>
            {/if}
            <span>페이지 {list.number + 1} / {list.totalPages || 1} (총 {list.totalElements}명)</span>
            {#if list.number < list.totalPages - 1}
              <button type="button" onclick={() => { page++; loadList(); }}>다음</button>
            {/if}
          </div>
        {:else if listError}
          <p class="list-error">
            {#if listErrorStatus === 401}
              세션이 만료되었거나 로그인되지 않았습니다. 다시 로그인한 뒤 새로고침하세요.
            {:else if listErrorStatus === 403}
              관리자 권한이 없습니다. 관리자 계정으로 로그인했는지 확인하세요.
            {:else if listErrorStatus && listErrorStatus >= 500}
              서버 오류가 발생했습니다. 잠시 후 다시 시도하세요.
            {:else}
              목록을 불러올 수 없습니다. 새로고침하거나 잠시 후 다시 시도하세요.
            {/if}
          </p>
          <button type="button" onclick={() => loadList()}>다시 불러오기</button>
        {:else if !listLoading}
          <p>회원이 없습니다.</p>
        {/if}
      </section>
    {/if}
  {:else}
    <p>로그인이 필요합니다.</p>
  {/if}
</div>

{#if emailUserId !== null}
  <div class="user-modal" role="dialog">
    <div class="user-modal-content">
      <h2>이메일 (AES256 복호화)</h2>
      <p>{emailValue ?? "로딩 중…"}</p>
      <button type="button" onclick={closeEmail}>닫기</button>
    </div>
  </div>
{/if}

{#if historyUserId !== null}
  <div class="user-modal" role="dialog">
    <div class="user-modal-content">
      <h2>비밀번호 변경 이력</h2>
      {#if historyData}
        <p>사용자: {historyData.username}</p>
        <ul>
          {#each historyData.content as h}
            <li>{new Date(h.changedAt).toLocaleString()}</li>
          {/each}
        </ul>
        <div class="history-pagination">
          {#if historyPage > 0}
            <button type="button" onclick={() => loadHistoryPage(historyPage - 1)}>이전</button>
          {/if}
          <span>페이지 {historyData.number + 1} / {historyData.totalPages || 1} (총 {historyData.totalElements}건)</span>
          {#if historyData.number < historyData.totalPages - 1}
            <button type="button" onclick={() => loadHistoryPage(historyPage + 1)}>다음</button>
          {/if}
        </div>
      {:else}
        <p>로딩 중…</p>
      {/if}
      <button type="button" onclick={closeHistory}>닫기</button>
    </div>
  </div>
{/if}
