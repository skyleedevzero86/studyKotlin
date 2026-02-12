<script setup lang="ts">
import { computed, unref } from 'vue'
import { useItemList } from '@/composables/useItemList'
import { useItemForm } from '@/composables/useItemForm'
import { useItemBulk } from '@/composables/useItemBulk'
import { useItemDelete } from '@/composables/useItemDelete'
import ItemFormModal from '@/components/ItemFormModal.vue'
import ItemBulkModal from '@/components/ItemBulkModal.vue'
import ItemDeleteModal from '@/components/ItemDeleteModal.vue'
import PaginationBar from '@/components/PaginationBar.vue'

const list = useItemList()

function refresh() {
  list.fetchPage()
}

const form = useItemForm(refresh)
const bulk = useItemBulk(refresh)
const del = useItemDelete(refresh)

const listState = computed(() => ({
  keyword: unref(list.keyword),
  items: unref(list.items),
  error: unref(list.error),
  loading: unref(list.loading),
  currentPage: unref(list.currentPage),
  totalPages: unref(list.totalPages),
  totalElements: unref(list.totalElements),
  size: unref(list.size),
  isFirst: unref(list.isFirst),
  isLast: unref(list.isLast),
}))
const formState = computed(() => ({
  open: unref(form.open),
  isEdit: unref(form.isEdit),
  name: unref(form.name),
  description: unref(form.description),
  loading: unref(form.loading),
  error: unref(form.error),
}))
const bulkState = computed(() => ({
  open: unref(bulk.open),
  rows: unref(bulk.rows),
  loading: unref(bulk.loading),
  error: unref(bulk.error),
}))
const delState = computed(() => ({
  open: unref(del.open),
  target: unref(del.target),
  loading: unref(del.loading),
  error: unref(del.error),
}))
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>항목 관리</h1>
      <div class="toolbar">
        <input
          type="search"
          :value="listState.keyword"
          @input="list.setKeyword(($event.target as HTMLInputElement).value)"
          placeholder="이름·설명 검색"
          class="search"
        />
        <button type="button" class="btn primary" @click="form.openCreate()">추가</button>
        <button type="button" class="btn success" @click="bulk.openModal()">리스트로 입력</button>
      </div>
    </header>

    <div v-if="listState.error" class="error-banner">{{ listState.error }}</div>
    <div v-if="listState.loading" class="loading">로딩 중...</div>

    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>이름</th>
            <th>설명</th>
            <th>등록일</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in listState.items" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.description }}</td>
            <td>{{ new Date(item.createdAt).toLocaleDateString('ko-KR') }}</td>
            <td class="actions">
              <button type="button" class="btn-sm success" @click="form.openEdit(item)">수정</button>
              <button type="button" class="btn-sm danger" @click="del.confirm(item)">삭제</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="listState.items.length === 0" class="empty">데이터가 없습니다.</p>
    </div>

    <PaginationBar
      :current-page="listState.currentPage"
      :total-pages="listState.totalPages"
      :total-elements="listState.totalElements"
      :size="listState.size"
      :is-first="listState.isFirst"
      :is-last="listState.isLast"
      @go-page="list.setPage"
    />

    <ItemFormModal
      :open="formState.open"
      :is-edit="formState.isEdit"
      :name="formState.name"
      :description="formState.description"
      :loading="formState.loading"
      :error="formState.error"
      @update:name="(v: string) => (form.name.value = v)"
      @update:description="(v: string) => (form.description.value = v)"
      @close="form.close()"
      @submit="form.submit()"
    />
    <ItemBulkModal
      :open="bulkState.open"
      :rows="bulkState.rows"
      :loading="bulkState.loading"
      :error="bulkState.error"
      @add-row="bulk.addRow()"
      @remove-row="bulk.removeRow($event)"
      @update-row="(index: number, field: 'name' | 'description', value: string) => bulk.updateRow(index, field, value)"
      @submit="bulk.submit()"
      @close="bulk.close()"
    />
    <ItemDeleteModal
      :open="delState.open"
      :target="delState.target"
      :loading="delState.loading"
      :error="delState.error"
      @confirm="del.doDelete()"
      @close="del.close()"
    />
  </div>
</template>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
}
.page-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}
.page-header h1 {
  margin: 0;
  font-size: 1.5rem;
}
.toolbar {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}
.search {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--border);
  border-radius: 4px;
  min-width: 200px;
  font: inherit;
}
.btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--bg);
  cursor: pointer;
  font: inherit;
}
.btn.primary {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}
.btn.success {
  background: var(--success);
  color: white;
  border-color: var(--success);
}
.error-banner {
  padding: 0.75rem;
  background: #fef2f2;
  color: var(--danger);
  border-radius: 4px;
  margin-bottom: 1rem;
}
.loading {
  padding: 2rem;
  text-align: center;
  color: var(--muted);
}
.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--border);
  border-radius: 8px;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th,
td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border);
}
th {
  font-weight: 600;
  background: var(--hover);
}
.actions {
  white-space: nowrap;
}
.btn-sm {
  padding: 0.25rem 0.5rem;
  margin-right: 0.25rem;
  font-size: 0.875rem;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--bg);
  cursor: pointer;
}
.btn-sm.success {
  background: var(--success);
  color: white;
  border-color: var(--success);
}
.btn-sm.danger {
  background: var(--danger);
  color: white;
  border-color: var(--danger);
}
.empty {
  padding: 2rem;
  text-align: center;
  color: var(--muted);
  margin: 0;
}
</style>
