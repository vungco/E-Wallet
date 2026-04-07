<script setup lang="ts">
import type { RecentRequestEntry, TransferStatusResponse, WalletResponse } from '~/types/api'

definePageMeta({ middleware: 'auth' })

const { apiFetch } = useApi()
const { setUserId } = useAuth()
const { load } = useRecentRequests()
const { connect } = useTransferSocket()

const entries = ref<RecentRequestEntry[]>([])
const statuses = ref<Record<string, TransferStatusResponse | undefined>>({})
const loadingId = ref<string | null>(null)
const error = ref('')
const bootError = ref('')

onMounted(async () => {
  connect()
  try {
    const w = await apiFetch<WalletResponse>('/api/v1/wallets')
    setUserId(w.userId)
    entries.value = load()
  } catch (e) {
    bootError.value = e instanceof Error ? e.message : 'Không xác định được user — không tải được lịch sử cục bộ'
  }
})

async function poll(requestId: string) {
  error.value = ''
  loadingId.value = requestId
  try {
    const s = await apiFetch<TransferStatusResponse>(`/api/v1/transactions/${encodeURIComponent(requestId)}`)
    statuses.value = { ...statuses.value, [requestId]: s }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Không tra được trạng thái'
  } finally {
    loadingId.value = null
  }
}
</script>

<template>
  <div class="mx-auto max-w-3xl px-4 py-10">
    <h1 class="text-3xl font-bold text-white">Giao dịch gần đây</h1>
    <p class="mt-1 text-slate-400">
      Lưu <span class="font-mono text-slate-300">requestId</span> theo từng tài khoản (trên máy bạn). Bấm “Tra cứu”
      để gọi <span class="font-mono text-slate-500">GET /api/v1/transactions/... </span>
    </p>

    <p v-if="bootError" class="mt-4 text-sm text-red-300">{{ bootError }}</p>
    <p v-if="error" class="mt-4 text-sm text-red-300">{{ error }}</p>

    <div v-if="!bootError && !entries.length" class="ew-card mt-8 p-8 text-center text-slate-400">
      Chưa có giao dịch nào được lưu. Thực hiện chuyển tiền từ
      <NuxtLink to="/transfer" class="text-teal-400 underline">trang chuyển tiền</NuxtLink>.
    </div>

    <ul v-else class="mt-8 space-y-4">
      <li v-for="e in entries" :key="e.requestId" class="ew-card p-5">
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div class="min-w-0">
            <p class="text-xs text-slate-500">{{ new Date(e.createdAt).toLocaleString() }}</p>
            <p class="mt-1 break-all font-mono text-sm text-slate-200">{{ e.requestId }}</p>
          </div>
          <button
            type="button"
            class="ew-btn shrink-0 py-2 text-sm"
            :disabled="loadingId === e.requestId"
            @click="poll(e.requestId)"
          >
            {{ loadingId === e.requestId ? 'Đang tra…' : 'Tra cứu' }}
          </button>
        </div>
        <div v-if="statuses[e.requestId]" class="mt-4 border-t border-white/10 pt-4 text-sm text-slate-300">
          <p>
            <span class="text-slate-500">Trạng thái:</span>
            <span class="ml-2 font-semibold text-white">{{ statuses[e.requestId]?.status }}</span>
          </p>
          <p v-if="statuses[e.requestId]?.errorMessage" class="mt-1 text-red-300">
            {{ statuses[e.requestId]?.errorMessage }}
          </p>
        </div>
      </li>
    </ul>
  </div>
</template>
