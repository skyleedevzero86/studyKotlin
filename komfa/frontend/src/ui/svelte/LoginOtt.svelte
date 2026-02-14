<script lang="ts">
  import { onMount } from "svelte";
  import "../css/Login.css";
  import { apiOrigin } from "../../infrastructure/http/api";

  let { goto }: { goto: (path: string) => void } = $props();

  let token = $state<string | null>(null);

  onMount(() => {
    if (typeof window === "undefined") return;
    const params = new URLSearchParams(window.location.search);
    const t = params.get("token")?.trim();
    token = t && t.length > 0 ? t : null;
  });
</script>

<div class="login-card ott-card">
  <h1>매직 링크 로그인</h1>
  {#if token}
    <p class="subtitle">아래 버튼을 눌러 로그인하세요.</p>
    <form
      id="ott-form"
      method="post"
      action="{apiOrigin()}/login/ott"
      class="ott-form"
    >
      <input type="hidden" name="token" value={token} />
      <button type="submit">로그인하기</button>
    </form>
  {:else}
    <p class="subtitle">유효한 토큰이 없습니다. 로그인 페이지에서 매직 링크를 다시 요청해 주세요.</p>
    <button type="button" class="btn-secondary" onclick={() => goto("/login")}>로그인으로</button>
  {/if}
</div>

<style>
  .ott-card .btn-secondary {
    width: 100%;
    padding: 0.6rem 1rem;
    background: #27272a;
    border: 1px solid #3f3f46;
    border-radius: 6px;
    color: #e4e4e7;
    font-size: 0.875rem;
    cursor: pointer;
  }
  .ott-card .btn-secondary:hover {
    background: #3f3f46;
  }
</style>
