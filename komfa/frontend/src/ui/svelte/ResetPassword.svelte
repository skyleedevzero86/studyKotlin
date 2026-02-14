<script lang="ts">
  import "../css/ResetPassword.css";
  import * as api from "../../infrastructure/http/api";

  let token = $state("");
  let newPassword = $state("");
  let message = $state<{ type: "ok" | "error"; text: string } | null>(null);
  let loading = $state(false);
  let done = $state(false);

  $effect(() => {
    if (typeof window === "undefined") return;
    const params = new URLSearchParams(window.location.search);
    token = params.get("token") ?? "";
  });

  async function submit(e: Event) {
    e.preventDefault();
    if (!token || !newPassword) {
      message = { type: "error", text: "비밀번호를 입력하세요." };
      return;
    }
    if (newPassword.length < 1) {
      message = { type: "error", text: "새 비밀번호를 입력하세요." };
      return;
    }
    message = null;
    loading = true;
    try {
      const result = await api.resetPassword(token, newPassword);
      if (result.ok) {
        done = true;
        message = {
          type: "ok",
          text: "비밀번호가 변경되었습니다. 로그인해 주세요.",
        };
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

<div class="reset-password-card">
  <h1>비밀번호 재설정</h1>
  {#if done}
    <p class="message" data-type="ok">{message?.text}</p>
    <a href="/login" class="link">로그인</a>
  {:else}
    <form onsubmit={submit}>
      <label for="reset-password">새 비밀번호</label>
      <input
        id="reset-password"
        type="password"
        bind:value={newPassword}
        placeholder="새 비밀번호"
        autocomplete="new-password"
        disabled={loading}
      />
      <button type="submit" disabled={loading || !token}
        >{loading ? "처리 중…" : "비밀번호 변경"}</button
      >
    </form>
    {#if message}
      <p class="message" data-type={message.type}>{message.text}</p>
    {/if}
    {#if !token}
      <p class="message" data-type="error">
        유효한 링크가 아닙니다. 비밀번호 찾기에서 다시 요청하세요.
      </p>
    {/if}
  {/if}
</div>
