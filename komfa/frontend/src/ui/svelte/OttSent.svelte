<script lang="ts">
  import "../css/OttSent.css";
  import type { OttSentProps } from "../ts/OttSent.types";

  let { onBack }: OttSentProps = $props();
</script>

<div class="ott-sent-card">
  <h2>전송 완료</h2>
  <p>
    이메일(또는 서버 로그)에서 매직 링크를 확인한 뒤, 해당 링크로 접속해 OTT
    인증을 완료하세요.
  </p>
  {#if onBack}
    <button onclick={onBack}>다시 요청</button>
  {/if}
</div>
