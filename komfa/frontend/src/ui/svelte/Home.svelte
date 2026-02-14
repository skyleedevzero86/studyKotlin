<script lang="ts">
  import "../css/Home.css";
  import type { MeResponse } from "../../infrastructure/http/api";
  import { apiOrigin } from "../../infrastructure/http/api";

  const logoutAction = $derived(apiOrigin() ? `${apiOrigin()}/logout` : "/logout");
  interface Props {
    profile: MeResponse;
    isAdmin: boolean;
    goto: (path: string) => void;
  }
  let { profile, isAdmin, goto }: Props = $props();
</script>

<div class="home-card">
  <h1>Komfa</h1>
  <p class="welcome">안녕하세요, <strong>{profile.username}</strong>님.</p>
  <nav class="nav">
    {#if isAdmin}
      <button type="button" onclick={() => goto("/user")}>회원 전용</button>
    {/if}
    <button type="button" onclick={() => goto("/me")}>마이페이지</button>
  </nav>
  <form method="post" action={logoutAction} class="logout-form">
    <button type="submit">로그아웃</button>
  </form>
</div>
