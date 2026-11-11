import { createRouter, createWebHistory } from 'vue-router';
import RunsView from './views/RunsView.vue';
import RunDetailView from './views/RunDetailView.vue';
import SubmissionsView from './views/SubmissionsView.vue';
import CdcView from './views/CdcView.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/runs' },
    { path: '/runs', component: RunsView, meta: { title: '이관 실행' } },
    { path: '/runs/:id', component: RunDetailView, props: true, meta: { title: '작업 상세와 검증 오류' } },
    { path: '/submissions', component: SubmissionsView, meta: { title: '심의 신청·첨부파일' } },
    { path: '/cdc', component: CdcView, meta: { title: '데이터 변경분 반영' } },
  ],
});
