<script setup lang="ts">
import type { NotificationItem } from '~/types/api'

definePageMeta({ middleware: 'auth' })

const route = useRoute()
const id = computed(() => Number(route.params.id))

const { fetchOne, refreshUnreadCount } = useNotifications()
const item = ref<NotificationItem | null>(null)
const error = ref('')
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    item.value = await fetchOne(id.value, true)
    await refreshUnreadCount()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Không tải được thông báo.'
  } finally {
    loading.value = false
  }
})

function formatWhen(iso: string | null) {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleString('vi-VN')
  } catch {
    return iso
  }
}
</script>

<template>
  <div class="mx-auto max-w-2xl px-4 py-10">
    <NuxtLink to="/notifications" class="text-sm text-teal-400 hover:text-teal-300">← Quay lại danh sách</NuxtLink>

    <div v-if="loading" class="mt-8 text-slate-500">Đang tải…</div>
    <div
      v-else-if="error"
      class="mt-8 rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200"
    >
      {{ error }}
    </div>
    <article v-else-if="item" class="mt-8 rounded-2xl border border-white/10 bg-white/5 p-6">
      <h1 class="text-xl font-semibold text-white">{{ item.title }}</h1>
      <p class="mt-2 text-xs text-slate-500">{{ formatWhen(item.createdAt) }}</p>
      <p class="mt-6 whitespace-pre-wrap text-slate-300">{{ item.body }}</p>
      <dl class="mt-8 grid gap-2 text-sm text-slate-400">
        <div v-if="item.transferStatus" class="flex gap-2">
          <dt class="text-slate-500">Trạng thái giao dịch</dt>
          <dd class="font-medium text-slate-200">{{ item.transferStatus }}</dd>
        </div>
        <div v-if="item.amount" class="flex gap-2">
          <dt class="text-slate-500">Số tiền</dt>
          <dd>{{ item.amount }}</dd>
        </div>
        <div v-if="item.requestId" class="flex gap-2">
          <dt class="text-slate-500">Request</dt>
          <dd class="font-mono text-xs">{{ item.requestId }}</dd>
        </div>
      </dl>
    </article>
  </div>
</template>
