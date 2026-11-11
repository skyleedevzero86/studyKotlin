<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { api, formatDate, query } from '../api';
import type { MigrationIssue, MigrationRun, VerificationReport } from '../types';
import { useNotice } from '../useNotice';
import NoticeBar from '../components/NoticeBar.vue';
import StatusBadge from '../components/StatusBadge.vue';

const props = defineProps<{ id: string }>();
const runInfo = ref<MigrationRun | null>(null);
const issues = ref<MigrationIssue[]>([]);
const report = ref<VerificationReport | null>(null);
const table = ref('');
const severity = ref('');
const { notice, busy, run } = useNotice();

const stagedLabels: Record<string, string> = { USER: '사용자', PROJECT: '과제', SUBMISSION: '심의 신청', REVIEW: '심의 이력' };
const findings = computed(() => report.value ? [
  ...report.value.missing.map((f) => ({ ...f, kind: '누락' })),
  ...report.value.duplicates.map((f) => ({ ...f, kind: '중복' })),
  ...report.value.wrongLinks.map((f) => ({ ...f, kind: '잘못된 연결' })),
] : []);

async function load() {
  const data = await run(() => api.get<MigrationRun>(`/api/migration-runs/${props.id}`));
  if (!data) return;
  runInfo.value = data;
  issues.value = (await run(() => api.get<MigrationIssue[]>(`/api/migration-runs/${props.id}/issues${query({ table: table.value, severity: severity.value })}`))) ?? [];
  report.value = (await run(() => api.get<VerificationReport>(`/api/migration-runs/${props.id}/verification`))) ?? null;
}

async function execute() {
  await run(() => api.post<MigrationRun>(`/api/migration-runs/${props.id}/execute`), (r) => (r.status === 'FAILED' ? `실패: ${r.errorMessage}` : `${r.statusLabel}`));
  await load();
}

watch([table, severity], load);
onMounted(load);
</script>

<template>
  <section v-if="runInfo" class="grid">
    <div class="panel">
      <h3>작업 {{ runInfo.id }} <StatusBadge :status="runInfo.status" :label="runInfo.statusLabel" /></h3>
      <NoticeBar :notice="notice" />
      <dl class="meta">
        <dt>방식</dt><dd>{{ runInfo.modeLabel }}</dd>
        <dt>설명</dt><dd>{{ runInfo.description }}</dd>
        <dt>실행 횟수</dt><dd>{{ runInfo.attemptCount }}회</dd>
        <dt>시작 / 종료</dt><dd>{{ formatDate(runInfo.startedAt) }} / {{ formatDate(runInfo.finishedAt) }}</dd>
        <dt>실패 사유</dt><dd class="error-text">{{ runInfo.errorMessage ?? '-' }}</dd>
        <dt>올린 자료</dt><dd><span v-for="(count, key) in runInfo.staged" :key="key" class="chip">{{ stagedLabels[key] ?? key }} {{ count }}줄</span></dd>
        <dt>새로 넣은 건수</dt><dd>사용자 {{ runInfo.inserted.users }} · 과제 {{ runInfo.inserted.projects }} · 신청 {{ runInfo.inserted.submissions }} · 이력 {{ runInfo.inserted.reviews }}</dd>
      </dl>
      <div class="actions">
        <button v-if="runInfo.mode === 'CSV'" class="primary" :disabled="busy" @click="execute">{{ runInfo.status === 'FAILED' ? '다시 처리' : '다시 실행' }}</button>
        <RouterLink to="/runs" class="button">목록</RouterLink>
      </div>
      <h3>오류 유형별 건수</h3>
      <table>
        <thead><tr><th>구분</th><th>유형</th><th>건수</th></tr></thead>
        <tbody>
          <tr v-for="item in runInfo.issueSummary" :key="item.issueType + item.severity">
            <td><StatusBadge :status="item.severity" :label="item.severity === 'ERROR' ? '넣지 않음' : '안내'" /></td>
            <td>{{ item.issueLabel }}</td><td>{{ item.count }}</td>
          </tr>
          <tr v-if="runInfo.issueSummary.length === 0"><td colspan="3" class="empty">검증 오류가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
    <div class="panel wide">
      <h3>최종 검증 <StatusBadge v-if="report" :status="report.passed ? 'COMPLETED' : 'FAILED'" :label="report.passed ? '통과' : '확인 필요'" /></h3>
      <table v-if="report">
        <thead><tr><th>자료</th><th>올린 줄</th><th>검증 오류로 뺀 줄</th><th>넣어야 할 건수</th><th>새 시스템에 있음</th><th>빠진 건수</th></tr></thead>
        <tbody>
          <tr v-for="row in report.tables" :key="row.tableName">
            <td>{{ row.tableLabel }}</td><td>{{ row.staged }}</td><td>{{ row.excluded }}</td><td>{{ row.expected }}</td><td>{{ row.loaded }}</td>
            <td :class="{ 'error-text': row.missing > 0 }">{{ row.missing }}</td>
          </tr>
        </tbody>
      </table>
      <table v-if="findings.length">
        <thead><tr><th>구분</th><th>자료</th><th>옛 시스템 번호</th><th>사유</th></tr></thead>
        <tbody><tr v-for="f in findings" :key="f.kind + f.sourceKey + f.reason"><td>{{ f.kind }}</td><td>{{ f.tableLabel }}</td><td class="mono">{{ f.sourceKey }}</td><td>{{ f.reason }}</td></tr></tbody>
      </table>
      <p v-else-if="report" class="hint">빠진 자료가 없습니다.<br>중복된 자료도 없습니다.<br>잘못 연결된 자료도 없습니다.</p>
      <h3>검증 오류</h3>
      <div class="toolbar">
        <select v-model="table">
          <option value="">전체 자료</option>
          <option value="stg_user">사용자</option><option value="stg_project">과제</option>
          <option value="stg_submission">심의 신청</option><option value="stg_review">심의 이력</option>
        </select>
        <select v-model="severity">
          <option value="">전체</option><option value="ERROR">넣지 않음</option><option value="INFO">안내</option>
        </select>
      </div>
      <table>
        <thead><tr><th>자료</th><th>줄</th><th>옛 시스템 번호</th><th>유형</th><th>사유</th></tr></thead>
        <tbody>
          <tr v-for="issue in issues" :key="issue.id">
            <td>{{ issue.tableLabel }}</td><td>{{ issue.rowNo }}</td><td class="mono">{{ issue.sourceKey }}</td>
            <td><StatusBadge :status="issue.severity" :label="issue.issueLabel" /></td><td>{{ issue.message }}</td>
          </tr>
          <tr v-if="issues.length === 0"><td colspan="5" class="empty">조건에 맞는 검증 오류가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </section>
  <NoticeBar v-else :notice="notice" />
</template>
