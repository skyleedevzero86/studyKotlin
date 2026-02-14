<script lang="ts">
  import { onMount } from "svelte";
  import "../css/Login.css";
  import type { LoginProps } from "../ts/Login.types";
  import { Username } from "../../domain/auth/types";
  import { requestOtt } from "../../application/auth/requestOtt";
  import { authApi } from "../../infrastructure/http/authApi";
  import { apiOrigin } from "../../infrastructure/http/api";

  let { onOttSent }: LoginProps = $props();
  const loginAction = $derived(apiOrigin() ? `${apiOrigin()}/login` : "/login");

  let username = $state("");
  let password = $state("");
  let step = $state<"password" | "ott">("password");
  let message = $state<{ type: "ok" | "error"; text: string } | null>(null);
  let loading = $state(false);

  onMount(() => {
    const params = typeof window !== "undefined" ? new URLSearchParams(window.location.search) : null;
    if (params?.get("error") != null) {
      message = { type: "error", text: "로그인에 실패했습니다. 사용자명·비밀번호를 확인하거나 관리자 승인 여부를 확인하세요." };
      if (typeof window !== "undefined") window.history.replaceState(null, "", window.location.pathname);
    }
  });

  function submitPasswordForm(e: Event) {
    e.preventDefault();
    if (!username.trim() || !password) {
      message = { type: "error", text: "사용자명과 비밀번호를 입력하세요." };
      return;
    }
    message = null;
    const form = e.target as HTMLFormElement;
    form.submit();
  }

  async function requestMagicLink(e: Event) {
    e.preventDefault();
    if (!username.trim()) {
      message = { type: "error", text: "사용자명을 입력하세요." };
      return;
    }
    loading = true;
    message = null;
    try {
      const result = await requestOtt(authApi, Username(username.trim()));
      if (result.ok) {
        message = {
          type: "ok",
          text: "이메일(또는 로그)에서 링크를 확인하세요.",
        };
        onOttSent?.();
      } else {
        message = { type: "error", text: result.error };
      }
    } catch (err) {
      message = {
        type: "error",
        text: err instanceof Error ? err.message : "요청 실패",
      };
    } finally {
      loading = false;
    }
  }
</script>

<div class="login-card">
  <h1>Komfa</h1>
  <p class="subtitle">2단계 인증 (MFA)</p>

  <form method="post" action={loginAction} onsubmit={submitPasswordForm}>
    <label for="username">사용자명</label>
    <input
      id="username"
      type="text"
      bind:value={username}
      name="username"
      placeholder="사용자명"
      autocomplete="username"
      disabled={loading}
    />
    <label for="password">비밀번호</label>
    <input
      id="password"
      type="password"
      bind:value={password}
      name="password"
      placeholder="비밀번호"
      autocomplete="current-password"
      disabled={loading}
    />
    <button type="submit" disabled={loading}>로그인</button>
  </form>

  <form onsubmit={requestMagicLink} class="ott-form">
    <label for="ott-username">매직 링크 받을 사용자명</label>
    <input
      id="ott-username"
      type="text"
      bind:value={username}
      placeholder="사용자명"
      disabled={loading}
    />
    <button type="submit" disabled={loading}>
      {loading ? "전송 중…" : "매직 링크 요청"}
    </button>
  </form>

  {#if message}
    <p class="message" data-type={message.type}>{message.text}</p>
  {/if}
</div>
