<script lang="ts">
  import "../css/User.css";
  import * as api from "../../infrastructure/http/api";

  let data = $state<{ message: string; username: string } | null>(null);

  $effect(() => {
    api.userArea().then((d) => {
      data = d;
    });
  });
</script>

<div class="user-card">
  <h1>회원 전용</h1>
  {#if data}
    <p>{data.message}</p>
    <p class="username">안녕하세요, <strong>{data.username}</strong>님.</p>
  {:else}
    <p>로그인이 필요합니다.</p>
  {/if}
</div>
