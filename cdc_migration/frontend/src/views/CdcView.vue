<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { api, formatDate, query } from '../api';
import type { CdcEvent, CdcEventPage, CdcRun } from '../types';
import { useNotice } from '../useNotice';
import NoticeBar from '../components/NoticeBar.vue';
import StatusBadge from '../components/StatusBadge.vue';
import HelpHeading from '../components/HelpHeading.vue';
import PageNav from '../components/PageNav.vue';

const PAGE_SIZE = 20;
const runs = ref<CdcRun[]>([]);
const current = ref<CdcRun | null>(null);
const events = ref<CdcEvent[]>([]);
const page = ref(1);
const totalPages = ref(1);
const total = ref(0);
const eventStatus = ref('');
const autoRefresh = ref(true);
const { notice, busy, run } = useNotice();
let timer: number | undefined;

const statusLabels: Record<string, string> = { STAGED: '받아 둠', PENDING: '처음 넣기 대기', APPLIED: '반영', WAITING: '다시 처리 대기', SKIPPED: '건너뜀', FAILED: '실패' };
const opLabels: Record<string, string> = { r: '처음 읽기', c: '등록', u: '수정', d: '삭제' };
const counts = computed(() => Object.entries(current.value?.eventCounts ?? {}));
const hasPendingWork = computed(() => ['WAITING', 'PENDING', 'FAILED'].some((key) => (current.value?.eventCounts?.[key] ?? 0) > 0));

async function loadRuns() {
  runs.value = (await api.get<CdcRun[]>('/api/cdc/runs').catch(() => [])) ?? [];
  const id = current.value?.id ?? runs.value[0]?.id;
  if (id) await select(id);
}

async function select(id: number) {
  const data = await run(() => api.get<CdcRun>(`/api/cdc/runs/${id}`));
  if (data) current.value = data;
  await loadEvents(id);
}

async function loadEvents(id: number) {
  const result = await api.get<CdcEventPage>(`/api/cdc/runs/${id}/events${query({ status: eventStatus.value, page: page.value, size: PAGE_SIZE })}`).catch(() => null);
  events.value = result?.items ?? [];
  page.value = result?.page ?? 1;
  totalPages.value = result?.totalPages ?? 1;
  total.value = result?.total ?? 0;
}

function goPage(target: number) {
  page.value = target;
  if (current.value) loadEvents(current.value.id);
}

function changeStatus() {
  page.value = 1;
  if (current.value) loadEvents(current.value.id);
}

async function start() {
  const created = await run(() => api.post<CdcRun>('/api/cdc/runs'), (r) => `${r.id}번 실행을 시작했습니다. 옛 시스템 자료를 처음부터 읽는 중입니다.`);
  if (created) {
    current.value = created;
    await loadRuns();
  }
}

async function action(path: string, success: string) {
  if (!current.value) return;
  await run(() => api.post<CdcRun>(`/api/cdc/runs/${current.value!.id}/${path}`), success);
  await select(current.value.id);
}

onMounted(async () => {
  await loadRuns();
  timer = window.setInterval(() => {
    if (autoRefresh.value && current.value) select(current.value.id);
  }, 3000);
});
onUnmounted(() => window.clearInterval(timer));
</script>

<template>
  <section class="grid">
    <div class="panel">
      <HelpHeading title="데이터 변경분 반영">
        먼저 옛 시스템의 자료를 처음부터 모두 읽습니다.<br>그다음 옛 시스템에서 바뀐 내용을 새 시스템에 바로 반영합니다.<br>이관 실행 화면에서 새 시스템 자료를 지운 뒤 시작하세요.
      </HelpHeading>
      <div class="actions">
        <button class="primary" :disabled="busy" @click="start">반영 시작</button>
        <button v-if="current && ['SNAPSHOTTING', 'LOADING', 'STREAMING'].includes(current.status)" class="danger" @click="action('stop', '반영을 멈췄습니다.')">멈추기</button>
        <label class="check"><input v-model="autoRefresh" type="checkbox" /> 3초마다 새로고침</label>
      </div>
      <NoticeBar :notice="notice" />
      <select v-if="runs.length" :value="current?.id" @change="select(Number(($event.target as HTMLSelectElement).value))">
        <option v-for="r in runs" :key="r.id" :value="r.id">{{ r.id }}번 실행 · {{ r.statusLabel }}</option>
      </select>
      <template v-if="current">
        <dl class="meta">
          <dt>상태</dt><dd><StatusBadge :status="current.status" :label="current.statusLabel" /></dd>
          <dt>연결 상태</dt><dd>{{ current.connectorState }}</dd>
          <dt>처음 읽기 끝남</dt><dd>{{ current.snapshotCompleted ? formatDate(current.snapshotCompletedAt) : '대기 중' }}</dd>
          <dt>처음 넣기</dt><dd>{{ current.initialLoadAt ? formatDate(current.initialLoadAt) : '-' }} · <RouterLink :to="`/runs/${current.migrationRunId}`">{{ current.migrationRunId }}번 작업</RouterLink> {{ current.migrationStatus }}</dd>
          <dt>두 번 받은 변경</dt><dd>{{ current.duplicateEvents }}건은 한 번만 반영했습니다.</dd>
          <dt>실패 사유</dt><dd class="error-text">{{ current.lastError ?? '-' }}</dd>
        </dl>
        <div v-if="current.initialLoadBlockers.length" class="notice notice-warn">
          <strong>처음 넣기를 기다리는 이유</strong>
          <ul><li v-for="b in current.initialLoadBlockers" :key="b">{{ b }}</li></ul>
        </div>
        <div class="actions end">
          <button v-if="current.status === 'FAILED' && !current.initialLoadAt" @click="action('retry-initial-load', '처음 넣기를 다시 시도합니다.')">처음 넣기 다시 하기</button>
          <button v-if="current.status === 'STREAMING'" @click="action('retry-waiting', '기다리던 변경을 다시 처리했습니다.')">기다리는 변경 지금 처리</button>
        </div>
        <h3>자료별 처음 읽기</h3>
        <table>
          <thead><tr><th>자료</th><th>읽기 끝남</th><th>읽어야 할 건수</th><th>받은 건수</th></tr></thead>
          <tbody>
            <tr v-for="t in current.snapshotTables" :key="t.tableName">
              <td>{{ t.label }}</td>
              <td><StatusBadge :status="t.scanCompleted ? 'COMPLETED' : 'PENDING'" :label="t.scanCompleted ? '완료' : '대기'" /></td>
              <td>{{ t.expectedRows ?? '-' }}</td>
              <td :class="{ 'error-text': t.expectedRows !== null && t.stagedRows < t.expectedRows }">{{ t.stagedRows }}</td>
            </tr>
          </tbody>
        </table>
        <h3>변경 처리 상태</h3>
        <div class="count-chips">
          <span v-for="[key, value] in counts" :key="key" class="count-chip">{{ statusLabels[key] ?? key }} <strong>{{ value }}</strong></span>
          <span v-if="counts.length === 0" class="count-empty">아직 받은 변경이 없습니다.</span>
        </div>
        <p v-if="counts.length && !hasPendingWork" class="count-note">새로 바뀐 내용이 없습니다.<br>지금까지 받은 건수만 보여 줍니다.</p>
      </template>
    </div>
    <div class="panel wide">
      <h3>변경 처리 기록</h3>
      <div class="toolbar">
        <select v-model="eventStatus" @change="changeStatus">
          <option value="">전체</option><option value="APPLIED">반영</option><option value="WAITING">다시 처리 대기</option>
          <option value="PENDING">처음 넣기 대기</option><option value="FAILED">실패</option><option value="SKIPPED">건너뜀</option><option value="STAGED">받아 둠</option>
        </select>
      </div>
      <table>
        <thead><tr><th>순번</th><th>자료</th><th>작업</th><th>옛 시스템 번호</th><th>상태</th><th>시도</th><th>중복</th><th>처리 내용</th><th>받은 시각</th></tr></thead>
        <tbody>
          <tr v-for="e in events" :key="e.eventId">
            <td>{{ e.eventId }}</td><td class="mono">{{ e.tableName }}</td><td>{{ opLabels[e.op.trim()] ?? e.op }}</td><td class="mono">{{ e.sourceKey }}</td>
            <td><StatusBadge :status="e.status" :label="statusLabels[e.status] ?? e.status" /></td><td>{{ e.attempts }}</td><td>{{ e.duplicateCount }}</td>
            <td class="sub">{{ e.message }}</td><td>{{ formatDate(e.receivedAt) }}</td>
          </tr>
          <tr v-if="events.length === 0"><td colspan="9" class="empty">변경 기록이 없습니다.</td></tr>
        </tbody>
      </table>
      <PageNav v-if="total > 0" :page="page" :total-pages="totalPages" :total="total" @go="goPage" />
    </div>
  </section>
</template>
