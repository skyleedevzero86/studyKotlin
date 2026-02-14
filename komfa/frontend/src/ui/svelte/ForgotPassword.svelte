<script lang="ts">
  import "../css/ForgotPassword.css";
  import * as api from "../../infrastructure/http/api";

  let email = $state("");
  let message = $state<{ type: "ok" | "error"; text: string } | null>(null);
  let loading = $state(false);

  async function submit(e: Event) {
    e.preventDefault();
    if (!email.trim()) {
      message = { type: "error", text: "이메일을 입력하세요." };
      return;
    }
    message = null;
    loading = true;
    try {
      const result = await api.forgotPassword(email.trim());
      if (result.ok) {
        message = { type: "ok", text: result.message };
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

<div class="forgot-password-card">
  <h1>비밀번호 찾기</h1>
  <form onsubmit={submit}>
    <label for="forgot-email">이메일</label>
    <input
      id="forgot-email"
      type="email"
      bind:value={email}
      placeholder="가입 시 등록한 이메일"
      disabled={loading}
    />
    <button type="submit" disabled={loading}
      >{loading ? "처리 중…" : "비밀번호 재설정 링크 받기"}</button
    >
  </form>
  {#if message}
    <p class="message" data-type={message.type}>{message.text}</p>
  {/if}
</div>
