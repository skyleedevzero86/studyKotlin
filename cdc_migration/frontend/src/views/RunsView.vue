<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { api, formatDate } from '../api';
import type { DemoFaults, MigrationRun } from '../types';
import { useNotice } from '../useNotice';
import NoticeBar from '../components/NoticeBar.vue';
import StatusBadge from '../components/StatusBadge.vue';
import HelpHeading from '../components/HelpHeading.vue';

const files = ref<Record<string, File | null>>({ users: null, projects: null, applications: null, reviews: null });
const labels: Record<string, string> = { users: '사용자 파일', projects: '과제 파일', applications: '심의 신청 파일', reviews: '심의 이력 파일' };
const templates: Record<string, string> = { users: '사용자-양식', projects: '과제-양식', applications: '심의신청-양식', reviews: '심의이력-양식' };
const description = ref('');
const runs = ref<MigrationRun[]>([]);
const faults = ref<DemoFaults>({ failNextMigration: false, failNextContentSave: false });
const { notice, busy, run } = useNotice();

async function load() {
  const data = await run(() => api.get<MigrationRun[]>('/api/migration-runs'));
  if (data) runs.value = data;
  const state = await api.get<DemoFaults>('/api/demo/faults').catch(() => null);
  if (state) faults.value = state;
}

function pick(key: string, event: Event) {
  files.value[key] = (event.target as HTMLInputElement).files?.[0] ?? null;
}

async function create() {
  const form = new FormData();
  for (const [key, file] of Object.entries(files.value)) {
    if (!file) {
      notice.value = { type: 'error', text: `${labels[key]} 파일을 선택하세요.` };
      return;
    }
    form.append(key, file);
  }
  if (description.value.trim()) form.append('description', description.value.trim());
  const created = await run(() => api.post<MigrationRun>('/api/migration-runs', form), (r) => `${r.id}번 작업을 만들었습니다. 올린 자료는 ${Object.values(r.staged).reduce((a, b) => a + b, 0)}줄입니다.`);
  if (created) await load();
}

async function execute(item: MigrationRun) {
  const result = await run(() => api.post<MigrationRun>(`/api/migration-runs/${item.id}/execute`), (r) =>
    r.status === 'FAILED' ? `${r.id}번 작업이 실패했습니다. ${r.errorMessage}` : `${r.id}번 작업 ${r.statusLabel}. 새로 넣은 건수는 사용자 ${r.inserted.users}, 과제 ${r.inserted.projects}, 신청 ${r.inserted.submissions}, 심의 이력 ${r.inserted.reviews}건입니다.`);
  if (result?.status === 'FAILED') notice.value = { type: 'error', text: `${result.id}번 작업이 실패했습니다. ${result.errorMessage}` };
  await load();
}

async function toggleFault() {
  const state = await run(() => api.put<DemoFaults>('/api/demo/faults', { failNextMigration: !faults.value.failNextMigration }));
  if (state) faults.value = state;
}

async function resetTarget() {
  if (!window.confirm('새 시스템에 옮긴 자료와 첨부파일, 처리 기록을 모두 지웁니다.\n계속할까요?')) return;
  await run(() => api.post<{ message: string }>('/api/admin/reset-target'), (r) => r.message);
  await load();
}

onMounted(load);
</script>

<template>
  <section class="grid">
    <div class="panel">
      <HelpHeading title="엑셀 데이터 이관">
        먼저 아래에서 파일 양식을 다운로드합니다.<br>양식에 옮길 자료를 직접 적고 저장합니다.<br>엑셀로 저장할 때는 엑셀 파일 인코딩을 고르세요.<br>파일 네 개를 모두 고른 뒤 작업 만들기를 누릅니다.
      </HelpHeading>
      <div class="actions pair">
        <a v-for="(name, key) in templates" :key="key" class="button" :href="`/templates/${key}.csv`" :download="`${name}.csv`">{{ labels[key].replace(' 파일', '') }} 양식 다운로드</a>
      </div>
      <div class="form">
        <label v-for="(label, key) in labels" :key="key">{{ label }}
          <span class="file-pick">
            <input type="file" accept=".csv" @change="pick(key, $event)" />
            <span class="file-pick-button">파일 선택</span>
            <span class="file-pick-name">{{ files[key]?.name ?? '선택한 파일이 없습니다' }}</span>
          </span>
        </label>
        <label>설명<input v-model="description" placeholder="예: 1차 옮기기 연습" /></label>
        <div class="actions end"><button class="go" :disabled="busy" @click="create">작업 만들기</button></div>
      </div>
      <HelpHeading title="연습 도구">
        실패를 예약하고 실행하면 넣던 자료가 모두 취소됩니다.<br>작업은 <strong class="em">실패</strong>로 남습니다.<br>같은 작업을 다시 실행하면 처음부터 다시 처리합니다.
      </HelpHeading>
      <div class="actions pair">
        <button :class="faults.failNextMigration ? 'danger' : ''" @click="toggleFault">
          {{ faults.failNextMigration ? '실패 예약 취소하기' : '다음 실행 한 번 실패시키기' }}
        </button>
        <button class="danger" @click="resetTarget">새 시스템 자료 지우기</button>
      </div>
    </div>
    <div class="panel wide">
      <h3>작업 목록</h3>
      <NoticeBar :notice="notice" />
      <table>
        <thead>
          <tr><th>번호</th><th>방식</th><th>상태</th><th>설명</th><th>실행</th><th>검증 오류</th><th>새로 넣은 건수</th><th>끝난 시각</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="item in runs" :key="item.id">
            <td><RouterLink :to="`/runs/${item.id}`">{{ item.id }}</RouterLink></td>
            <td>{{ item.modeLabel }}</td>
            <td><StatusBadge :status="item.status" :label="item.statusLabel" /></td>
            <td>{{ item.description }}<div v-if="item.errorMessage" class="error-text">{{ item.errorMessage }}</div></td>
            <td>{{ item.attemptCount }}회</td>
            <td>{{ item.errorCount }}건<div v-if="item.infoCount" class="sub">안내 {{ item.infoCount }}건</div></td>
            <td class="sub">사용자 {{ item.inserted.users }} · 과제 {{ item.inserted.projects }} · 신청 {{ item.inserted.submissions }} · 이력 {{ item.inserted.reviews }}</td>
            <td>{{ formatDate(item.finishedAt) }}</td>
            <td>
              <button v-if="item.mode === 'CSV'" :disabled="busy || item.status === 'RUNNING'" @click="execute(item)">
                {{ item.status === 'CREATED' ? '실행' : item.status === 'FAILED' ? '다시 처리' : '다시 실행' }}
              </button>
            </td>
          </tr>
          <tr v-if="runs.length === 0"><td colspan="9" class="empty">작업이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
