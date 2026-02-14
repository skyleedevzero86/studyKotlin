<script lang="ts">
  import "../css/Me.css";
  import * as api from "../../infrastructure/http/api";

  let profile = $state<api.MeResponse | null>(null);
  let emailEdit = $state("");
  let message = $state<{ type: "ok" | "error"; text: string } | null>(null);
  let loading = $state(false);
  let tab = $state<"profile" | "password" | "withdraw">("profile");
  let currentPassword = $state("");
  let newPassword = $state("");

  async function load() {
    const data = await api.me();
    profile = data;
    if (data) emailEdit = data.email ?? "";
  }

  $effect(() => {
    load();
  });

  async function saveProfile(e: Event) {
    e.preventDefault();
    message = null;
    loading = true;
    try {
      const result = await api.updateProfile(emailEdit.trim() || null);
      if (result.ok) {
        message = { type: "ok", text: "저장되었습니다." };
        await load();
      } else {
        message = { type: "error", text: "저장에 실패했습니다." };
      }
    } catch {
      message = { type: "error", text: "요청 실패" };
    } finally {
      loading = false;
    }
  }

  async function changePasswordSubmit(e: Event) {
    e.preventDefault();
    if (!currentPassword || !newPassword) {
      message = {
        type: "error",
        text: "현재 비밀번호와 새 비밀번호를 입력하세요.",
      };
      return;
    }
    message = null;
    loading = true;
    try {
      const result = await api.changePassword(currentPassword, newPassword);
      if (result.ok) {
        message = { type: "ok", text: "비밀번호가 변경되었습니다." };
        currentPassword = "";
        newPassword = "";
      } else {
        message = { type: "error", text: "비밀번호 변경에 실패했습니다." };
      }
    } catch {
      message = { type: "error", text: "요청 실패" };
    } finally {
      loading = false;
    }
  }

  async function withdrawSubmit(e: Event) {
    e.preventDefault();
    if (
      !confirm(
        "정말 탈퇴하시겠습니까? 같은 아이디/이메일로 재가입할 수 없습니다.",
      )
    )
      return;
    message = null;
    loading = true;
    try {
      const result = await api.withdraw();
      if (result.ok) {
        message = { type: "ok", text: result.message };
        window.location.href = "/";
      } else {
        message = { type: "error", text: result.message };
      }
    } catch {
      message = { type: "error", text: "요청 실패" };
    } finally {
      loading = false;
    }
  }
</script>

<div class="me-card">
  <h1>마이페이지</h1>
  {#if !profile}
    <p>로그인이 필요합니다.</p>
  {:else}
    <div class="tabs">
      <button
        type="button"
        class:active={tab === "profile"}
        onclick={() => {
          tab = "profile";
          message = null;
        }}>프로필</button
      >
      <button
        type="button"
        class:active={tab === "password"}
        onclick={() => {
          tab = "password";
          message = null;
        }}>비밀번호 변경</button
      >
      <button
        type="button"
        class:active={tab === "withdraw"}
        onclick={() => {
          tab = "withdraw";
          message = null;
        }}>탈퇴</button
      >
    </div>

    {#if tab === "profile"}
      <form onsubmit={saveProfile}>
        <span class="label">아이디</span>
        <p class="readonly">{profile.username}</p>
        <label for="me-email">이메일</label>
        <input
          id="me-email"
          type="email"
          bind:value={emailEdit}
          disabled={loading}
        />
        <button type="submit" disabled={loading}
          >{loading ? "저장 중…" : "저장"}</button
        >
      </form>
    {:else if tab === "password"}
      <form onsubmit={changePasswordSubmit}>
        <label for="me-current">현재 비밀번호</label>
        <input
          id="me-current"
          type="password"
          bind:value={currentPassword}
          disabled={loading}
        />
        <label for="me-new">새 비밀번호</label>
        <input
          id="me-new"
          type="password"
          bind:value={newPassword}
          disabled={loading}
        />
        <button type="submit" disabled={loading}
          >{loading ? "처리 중…" : "비밀번호 변경"}</button
        >
      </form>
    {:else if tab === "withdraw"}
      <form onsubmit={withdrawSubmit}>
        <p class="warn">탈퇴 시 같은 아이디·이메일로 재가입할 수 없습니다.</p>
        <button type="submit" disabled={loading} class="danger"
          >{loading ? "처리 중…" : "탈퇴하기"}</button
        >
      </form>
    {/if}

    {#if message}
      <p class="message" data-type={message.type}>{message.text}</p>
    {/if}
  {/if}
</div>
