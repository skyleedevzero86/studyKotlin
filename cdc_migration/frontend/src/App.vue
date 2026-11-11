<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { saveWorker, worker } from './api';

const route = useRoute();
const title = computed(() => (route.meta.title as string | undefined) ?? '카프카 마이그레이션');
const links = [
  { to: '/runs', label: '이관 실행' },
  { to: '/submissions', label: '심의 신청·첨부파일' },
  { to: '/cdc', label: '데이터 변경분 반영' },
];
</script>

<template>
  <div class="app">
    <header class="topbar">
      <div>
        <h1><RouterLink to="/" class="home-link">카프카 마이그레이션</RouterLink></h1>
        <p class="sub">합성 데이터로 엑셀 데이터 일회성 이관, 데이터 변경분 반영을 확인합니다.</p>
      </div>
      <label class="worker">
        작업자
        <input :value="worker" placeholder="작업 기록에 남을 이름" @input="saveWorker(($event.target as HTMLInputElement).value)" />
      </label>
    </header>
    <nav class="tabs">
      <RouterLink v-for="link in links" :key="link.to" :to="link.to" active-class="active">{{ link.label }}</RouterLink>
    </nav>
    <h2 class="page-title">{{ title }}</h2>
    <RouterView />
  </div>
</template>
