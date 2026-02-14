<script lang="ts">
  import { onMount } from "svelte";
  import "../css/User.css";
  import * as api from "../../infrastructure/http/api";
  import { statusLabel } from "../ts/Admin";

  let {
    profile = null,
    isAdmin = false,
  }: { profile?: api.MeResponse | null; isAdmin?: boolean } = $props();

  let data = $state<{ message: string; username: string } | null>(null);
  let list = $state<api.AdminUserListResponse | null>(null);
  let listLoading = $state(false);
  let listError = $state(false);
  let page = $state(0);
  let size = 20;
  let search = $state("");
  let emailUserId = $state<number | null>(null);
  let emailValue = $state<string | null>(null);

  $effect(() => {
    if (profile) {
      api.userArea().then((d) => {
        data = d;
      });
    } else {
      data = null;
    }
  });

  async function loadList() {
    listLoading = true;
    listError = false;
    try {
      const res = await api.adminUsersList(page, size, search.trim() || null);
      list = res;
      if (!res) listError = true;
    } finally {
      listLoading = false;
    }
  }

  $effect(() => {
    if (profile) loadList();
    else list = null;
  });

  onMount(() => {
    if (profile && !list && !listLoading) loadList();
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

  async function changeRole(
    userId: number,
    newRole: "ROLE_USER" | "ROLE_ADMIN",
  ) {
    const res = await api.adminUsersUpdateRole(userId, newRole);
    if (!("error" in res)) loadList();
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

    {#if list !== null || listLoading || listError}
      <section class="member-list">
        <h2>가입 회원 목록</h2>
        <form onsubmit={doSearch} class="search-form">
          <input type="text" bind:value={search} placeholder="아이디 검색" />
          <button type="submit">검색</button>
        </form>
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
                        title="더블클릭 시 복호화된 이메일 표시"
                        ondblclick={() => showDecryptedEmail(u.id)}
                        >{u.emailMasked}</button
                      >
                    {:else}
                      -
                    {/if}
                  </td>
                  <td class="roles-cell">
                    <span class="role-text"
                      >{u.roles.includes("ROLE_ADMIN")
                        ? "관리자"
                        : "일반회원"}</span
                    >
                    {#if u.status !== "WITHDRAWN"}
                      {#if u.roles.includes("ROLE_ADMIN")}
                        <button
                          type="button"
                          class="btn-sm"
                          onclick={() => changeRole(u.id, "ROLE_USER")}
                          >일반회원으로</button
                        >
                      {:else}
                        <button
                          type="button"
                          class="btn-sm"
                          onclick={() => changeRole(u.id, "ROLE_ADMIN")}
                          >관리자로</button
                        >
                      {/if}
                    {/if}
                  </td>
                  <td>{statusLabel(u.status)}</td>
                  <td>{new Date(u.createdAt).toLocaleDateString()}</td>
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
        {:else if listError}
          <p class="list-error">
            목록을 불러올 수 없습니다. 권한을 확인하거나 새로고침 후 다시
            시도하세요.
          </p>
          <button type="button" onclick={() => loadList()}>다시 불러오기</button
          >
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
