<script lang="ts">
  import "../css/Join.css";
  import * as api from "../../infrastructure/http/api";

  let username = $state("");
  let password = $state("");
  let passwordConfirm = $state("");
  let email = $state("");
  let message = $state<{ type: "ok" | "error"; text: string } | null>(null);
  let loading = $state(false);

  async function handleSubmit(e: Event) {
    e.preventDefault();
    message = null;

    if (!username.trim()) {
      message = { type: "error", text: "사용자명을 입력하세요." };
      return;
    }
    if (!password) {
      message = { type: "error", text: "비밀번호를 입력하세요." };
      return;
    }
    if (password !== passwordConfirm) {
      message = { type: "error", text: "비밀번호가 일치하지 않습니다." };
      return;
    }

    loading = true;
    try {
      const result = await api.join(
        username.trim(),
        password,
        email.trim() || null,
      );
      if (result.ok) {
        message = {
          type: "ok",
          text: "회원가입이 완료되었습니다. 로그인해 주세요.",
        };
        username = "";
        password = "";
        passwordConfirm = "";
        email = "";
      } else if (!result.ok && result.error) {
        message = { type: "error", text: result.error.message };
      } else if (result.status === 400) {
        message = { type: "error", text: "입력값이 올바르지 않습니다." };
      } else {
        message = { type: "error", text: "회원가입에 실패했습니다." };
      }
    } catch {
      message = { type: "error", text: "요청 중 오류가 발생했습니다." };
    } finally {
      loading = false;
    }
  }
</script>

<div class="join-card">
  <h1>회원가입</h1>

  <form onsubmit={handleSubmit}>
    <label for="join-username">사용자명</label>
    <input
      id="join-username"
      type="text"
      bind:value={username}
      placeholder="사용자명"
      autocomplete="username"
      disabled={loading}
    />

    <label for="join-password">비밀번호</label>
    <input
      id="join-password"
      type="password"
      bind:value={password}
      placeholder="비밀번호"
      autocomplete="new-password"
      disabled={loading}
    />

    <label for="join-password-confirm">비밀번호 확인</label>
    <input
      id="join-password-confirm"
      type="password"
      bind:value={passwordConfirm}
      placeholder="비밀번호 확인"
      autocomplete="new-password"
      disabled={loading}
    />

    <label for="join-email">이메일 (선택)</label>
    <input
      id="join-email"
      type="email"
      bind:value={email}
      placeholder="예: user@example.com"
      autocomplete="email"
      disabled={loading}
    />

    <button type="submit" disabled={loading}>
      {loading ? "처리 중…" : "가입하기"}
    </button>
  </form>

  {#if message}
    <p class="message" data-type={message.type}>{message.text}</p>
  {/if}
</div>
