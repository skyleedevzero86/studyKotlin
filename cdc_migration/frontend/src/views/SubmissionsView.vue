<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { api, formatBytes, formatDate, query } from '../api';
import type { Attachment, AttachmentVerification, DemoFaults, FileTransfer, Review, SubmissionDetail, SubmissionSummary, TransferBatch } from '../types';
import { useNotice } from '../useNotice';
import NoticeBar from '../components/NoticeBar.vue';
import StatusBadge from '../components/StatusBadge.vue';
import HelpHeading from '../components/HelpHeading.vue';

const list = ref<SubmissionSummary[]>([]);
const instCode = ref('');
const status = ref('');
const keyword = ref('');
const selected = ref<SubmissionDetail | null>(null);
const reviews = ref<Review[]>([]);
const attachments = ref<Attachment[]>([]);
const verification = ref<Record<number, AttachmentVerification>>({});
const uploadFile = ref<File | null>(null);
const requestId = ref(newRequestId());
const faults = ref<DemoFaults>({ failNextMigration: false, failNextContentSave: false });
const transfers = ref<FileTransfer[]>([]);
const transferStatus = ref('UNMAPPED');
const { notice, busy, run } = useNotice();
const detailNotice = useNotice();

function newRequestId(): string {
  return `req-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
}

async function search() {
  list.value = (await run(() => api.get<SubmissionSummary[]>(`/api/submissions${query({ instCode: instCode.value, status: status.value, keyword: keyword.value })}`))) ?? list.value;
}

async function open(subNo: number) {
  const data = await detailNotice.run(() => api.get<{ submission: SubmissionDetail; reviews: Review[] }>(`/api/submissions/${subNo}`));
  if (!data) return;
  selected.value = data.submission;
  reviews.value = data.reviews;
  verification.value = {};
  await loadAttachments();
}

async function loadAttachments() {
  if (!selected.value) return;
  attachments.value = (await detailNotice.run(() => api.get<Attachment[]>(`/api/submissions/${selected.value!.subNo}/attachments`))) ?? [];
}

async function upload() {
  if (!selected.value || !uploadFile.value) {
    detailNotice.notice.value = { type: 'error', text: '올릴 파일을 선택하세요.' };
    return;
  }
  const form = new FormData();
  form.append('file', uploadFile.value);
  const saved = await detailNotice.run(
    () => api.post<Attachment>(`/api/submissions/${selected.value!.subNo}/attachments`, form, { 'X-Request-Id': requestId.value }),
    (a) => `${a.id}번 첨부 ${a.statusLabel}. 요청 번호는 ${a.requestId}입니다.`,
  );
  if (saved) requestId.value = newRequestId();
  await loadAttachments();
  await loadFaults();
  await search();
}

async function verify(id: number) {
  const result = await detailNotice.run(() => api.post<AttachmentVerification>(`/api/attachments/${id}/verify`));
  if (result) verification.value = { ...verification.value, [id]: result };
}

async function loadFaults() {
  const state = await api.get<DemoFaults>('/api/demo/faults').catch(() => null);
  if (state) faults.value = state;
}

async function toggleContentFault() {
  const state = await detailNotice.run(() => api.put<DemoFaults>('/api/demo/faults', { failNextContentSave: !faults.value.failNextContentSave }));
  if (state) faults.value = state;
}

async function importTransfers(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;
  const form = new FormData();
  form.append('file', file);
  await run(() => api.post<TransferBatch>('/api/file-transfers/csv', form), (r) => `첨부파일 ${r.total}건을 옮겼습니다. 완료 ${r.done}건, 신청을 못 찾음 ${r.unmapped}건, 실패 ${r.failed}건입니다.`);
  (event.target as HTMLInputElement).value = '';
  await loadTransfers();
  await search();
}

async function loadTransfers() {
  transfers.value = (await api.get<FileTransfer[]>(`/api/file-transfers${query({ status: transferStatus.value })}`).catch(() => [])) ?? [];
}

async function retryTransfers() {
  await run(() => api.post<{ processed: number }>('/api/file-transfers/process'), (r) => `${r.processed}건 다시 처리했습니다.`);
  await loadTransfers();
  await search();
}

onMounted(async () => {
  await search();
  await loadFaults();
  await loadTransfers();
});
</script>

<template>
  <section class="grid">
    <div class="panel wide">
      <h3>심의 신청</h3>
      <NoticeBar :notice="notice" />
      <div class="toolbar">
        <select v-model="instCode"><option value="">전체 기관</option><option value="H01">H01</option><option value="H02">H02</option></select>
        <select v-model="status">
          <option value="">전체 상태</option><option value="RECEIVED">접수</option><option value="UNDER_REVIEW">심의중</option>
          <option value="APPROVED">승인</option><option value="REJECTED">반려</option><option value="WITHDRAWN">취소</option>
        </select>
        <input v-model="keyword" placeholder="제목·구 신청번호·새 신청번호" @keyup.enter="search" />
        <button @click="search">검색</button>
      </div>
      <table>
        <thead><tr><th>새 신청번호</th><th>기관 / 옛 신청번호</th><th>제목</th><th>상태</th><th>과제</th><th>신청자</th><th>이력</th><th>첨부</th><th>들어온 방법</th></tr></thead>
        <tbody>
          <tr v-for="item in list" :key="item.subNo" :class="{ selected: selected?.subNo === item.subNo, muted: item.deleted }" @click="open(item.subNo)">
            <td>{{ item.subNo }}</td><td class="mono">{{ item.instCode }} / {{ item.regnoold }}</td><td>{{ item.title }}</td>
            <td><StatusBadge :status="item.status" :label="item.deleted ? '삭제됨' : item.statusLabel" /></td>
            <td class="mono">{{ item.projectNo }}</td><td>{{ item.applicantName }}</td><td>{{ item.reviewCount }}</td><td>{{ item.attachmentCount }}</td><td>{{ item.source }}</td>
          </tr>
          <tr v-if="list.length === 0"><td colspan="9" class="empty">새 시스템에 신청이 없습니다.<br>이관 실행 화면에서 먼저 자료를 옮기세요.</td></tr>
        </tbody>
      </table>
      <div class="section-head">
        <HelpHeading title="첨부파일 옮기기">
          먼저 첨부파일 양식을 다운로드합니다.<br>양식에 옮길 파일 목록을 직접 적고 저장합니다.<br>저장한 파일을 올리면 옛 시스템의 첨부파일을 새 시스템으로 옮깁니다.<br>신청을 찾지 못한 파일은 <strong class="em">신청을 못 찾음</strong>으로 남습니다.<br>나중에 그 신청이 옮겨지면 자동으로 다시 시도합니다.
        </HelpHeading>
        <div class="section-tools">
          <a class="button download" href="/templates/attachments.csv" download="첨부파일-양식.csv">첨부파일 양식 다운로드</a>
          <label class="upload-button">
            <input type="file" accept=".csv" @change="importTransfers" />
            <span>파일 올리기</span>
          </label>
        </div>
      </div>
      <div class="toolbar">
        <select v-model="transferStatus" @change="loadTransfers">
          <option value="">전체</option><option value="UNMAPPED">신청을 못 찾음</option><option value="DONE">완료</option><option value="FAILED">실패</option><option value="PENDING">대기</option>
        </select>
        <button @click="retryTransfers">지금 다시 처리</button>
      </div>
      <table>
        <thead><tr><th>번호</th><th>들어온 방법</th><th>옛 기관 / 파일</th><th>옛 신청번호</th><th>파일 이름</th><th>상태</th><th>시도</th><th>사유</th></tr></thead>
        <tbody>
          <tr v-for="t in transfers" :key="t.transferId">
            <td>{{ t.transferId }}</td><td>{{ t.source }}</td><td class="mono">{{ t.instCode }}/{{ t.legacyFileId }}</td><td>{{ t.legacyRegno }}</td>
            <td>{{ t.fileName }}</td><td><StatusBadge :status="t.status" :label="t.statusLabel" /></td><td>{{ t.attempts }}</td><td class="sub">{{ t.lastError }}</td>
          </tr>
          <tr v-if="transfers.length === 0"><td colspan="8" class="empty">이 상태의 작업이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
    <div class="panel">
      <template v-if="selected">
        <h3>신청 {{ selected.subNo }} <StatusBadge :status="selected.deleted ? 'FAILED' : 'COMPLETED'" :label="selected.deleted ? '삭제됨' : selected.statusLabel" /></h3>
        <NoticeBar :notice="detailNotice.notice.value" />
        <dl class="meta">
          <dt>옛 기관 / 신청번호</dt><dd class="mono">{{ selected.instCode }} / {{ selected.regnoold }}</dd>
          <dt>제목</dt><dd>{{ selected.title }}</dd>
          <dt>과제</dt><dd>{{ selected.projectNo }} {{ selected.projectTitle }}<span v-if="selected.virtualProject" class="chip">임시 과제</span></dd>
          <dt>신청자</dt><dd>{{ selected.applicantName }} · {{ selected.applicantUserId }}</dd>
          <dt>신청 / 수정</dt><dd>{{ formatDate(selected.submittedAt) }} / {{ formatDate(selected.updatedAt) }}</dd>
          <dt>들어온 방법</dt><dd>{{ selected.source }} · {{ selected.migrationRunId ?? '-' }}번 작업 · {{ selected.version }}번째 수정</dd>
        </dl>
        <h3>심의 이력</h3>
        <table>
          <thead><tr><th>옛 심의 번호</th><th>결과</th><th>심의위원</th><th>일시</th></tr></thead>
          <tbody>
            <tr v-for="r in reviews" :key="r.reviewNo"><td class="mono">{{ r.legacyReviewId }}</td><td>{{ r.resultLabel }}</td><td>{{ r.reviewerName }}</td><td>{{ formatDate(r.reviewedAt) }}</td></tr>
            <tr v-if="reviews.length === 0"><td colspan="4" class="empty">심의 이력이 없습니다.</td></tr>
          </tbody>
        </table>
        <HelpHeading title="첨부파일">
          파일은 10MB까지 올릴 수 있습니다.<br>저장에 실패하면 같은 요청 번호로 다시 올리세요.<br>그러면 이어서 저장합니다.
        </HelpHeading>
        <div class="form">
          <label>파일
            <span class="file-pick">
              <input type="file" @change="uploadFile = ($event.target as HTMLInputElement).files?.[0] ?? null" />
              <span class="file-pick-button">파일 선택</span>
              <span class="file-pick-name">{{ uploadFile?.name ?? '선택한 파일이 없습니다' }}</span>
            </span>
          </label>
          <label>요청 번호<input v-model="requestId" class="mono" /></label>
          <div class="actions end">
            <button class="primary" :disabled="busy" @click="upload">올리기</button>
            <button :class="faults.failNextContentSave ? 'danger' : ''" @click="toggleContentFault">
              {{ faults.failNextContentSave ? '한 번 실패하기 취소' : '한 번 실패하기' }}
            </button>
          </div>
        </div>
        <div v-for="a in attachments" :key="a.id" class="card">
          <div class="card-head">
            <strong>{{ a.fileName }}</strong>
            <StatusBadge :status="a.status" :label="a.statusLabel" />
          </div>
          <div class="sub">요청 번호 {{ a.requestId }} · 크기 {{ formatBytes(a.byteSize) }}</div>
          <div v-if="a.originFileId" class="sub">옛 파일 {{ a.originInstCode }} / {{ a.originFileId }} · {{ a.originSeq }}번째 파일</div>
          <div v-if="a.lastError" class="error-text">{{ a.lastError }}</div>
          <div class="actions">
            <a class="button" :href="`/api/attachments/${a.id}/download`">다운로드</a>
            <button @click="verify(a.id)">데이터 비교하기</button>
            <button v-if="a.status === 'FAILED'" @click="requestId = a.requestId">이 요청 번호로 다시 올리기</button>
          </div>
          <div v-if="verification[a.id]" :class="['notice', verification[a.id].matches ? 'notice-ok' : 'notice-error']">
            <template v-if="verification[a.id].matches">두 곳에 저장된 파일이 <strong class="em">같습니다</strong>.</template>
            <template v-else>두 곳에 저장된 파일이 <strong class="em">다릅니다</strong>.<br>{{ verification[a.id].message }}</template>
          </div>
        </div>
        <p v-if="attachments.length === 0" class="empty">첨부파일이 없습니다.</p>
      </template>
      <p v-else class="empty">왼쪽 목록에서 신청을 고르세요.</p>
    </div>
  </section>
</template>
