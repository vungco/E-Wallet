<script setup lang="ts">
import type { WalletFundRequest, WalletOperationResponse, WalletResponse } from '~/types/api'

definePageMeta({ middleware: 'auth' })

const { apiFetch } = useApi()
const { setUserId } = useAuth()
const { status: wsStatus, connect: wsConnect } = useTransferSocket()

const wallet = ref<WalletResponse | null>(null)
const loadError = ref('')
const loading = ref(true)

const depositAmount = ref('')
const withdrawAmount = ref('')
const fundLoading = ref(false)
const fundMsg = ref('')

async function loadWallet() {
  loadError.value = ''
  loading.value = true
  try {
    const w = await apiFetch<WalletResponse>('/api/v1/wallets')
    wallet.value = w
    setUserId(w.userId)
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : 'Không tải được ví'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadWallet()
  wsConnect()
})

async function deposit() {
  fundMsg.value = ''
  fundLoading.value = true
  try {
    const amt = Number(String(depositAmount.value).replace(',', '.'))
    if (!Number.isFinite(amt) || amt <= 0) {
      fundMsg.value = 'Số tiền không hợp lệ'
      return
    }
    const body: WalletFundRequest = {
      amount: amt,
      idempotencyKey: crypto.randomUUID(),
    }
    const res = await apiFetch<WalletOperationResponse>('/api/v1/wallets/deposit', {
      method: 'POST',
      body: JSON.stringify(body),
    })
    fundMsg.value = `Nạp thành công · Số dư: ${res.balanceAfter}`
    depositAmount.value = ''
    await loadWallet()
  } catch (e) {
    fundMsg.value = e instanceof Error ? e.message : 'Lỗi nạp tiền'
  } finally {
    fundLoading.value = false
  }
}

async function withdraw() {
  fundMsg.value = ''
  fundLoading.value = true
  try {
    const amt = Number(String(withdrawAmount.value).replace(',', '.'))
    if (!Number.isFinite(amt) || amt <= 0) {
      fundMsg.value = 'Số tiền không hợp lệ'
      return
    }
    const body: WalletFundRequest = {
      amount: amt,
      idempotencyKey: crypto.randomUUID(),
    }
    const res = await apiFetch<WalletOperationResponse>('/api/v1/wallets/withdraw', {
      method: 'POST',
      body: JSON.stringify(body),
    })
    fundMsg.value = `Rút thành công · Số dư: ${res.balanceAfter}`
    withdrawAmount.value = ''
    await loadWallet()
  } catch (e) {
    fundMsg.value = e instanceof Error ? e.message : 'Lỗi rút tiền'
  } finally {
    fundLoading.value = false
  }
}

const wsStatusLabel = computed(() => {
  switch (wsStatus.value) {
    case 'open':
      return 'Đã kết nối real-time'
    case 'connecting':
      return 'Đang kết nối WS…'
    case 'error':
      return 'Lỗi WS (thử lại khi chuyển tiền)'
    case 'closed':
      return 'WS đã đóng'
    default:
      return 'WS chưa kết nối'
  }
})
</script>

<template>
  <div class="mx-auto max-w-4xl px-4 py-10">
    <div class="mb-8 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold tracking-tight text-white">Tổng quan</h1>
        <p class="text-slate-400">Số dư và thao tác nhanh</p>
      </div>
      <div
        class="inline-flex items-center gap-2 rounded-full border border-white/10 bg-slate-900/50 px-3 py-1.5 text-xs text-slate-400"
      >
        <span
          class="h-2 w-2 rounded-full"
          :class="
            wsStatus === 'open'
              ? 'bg-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.8)]'
              : 'bg-amber-500/80'
          "
        />
        {{ wsStatusLabel }}
      </div>
    </div>

    <div v-if="loading" class="text-slate-400">Đang tải ví…</div>
    <p v-else-if="loadError" class="text-red-300">{{ loadError }}</p>

    <div v-else-if="wallet" class="grid gap-6 lg:grid-cols-2">
      <div class="ew-card relative overflow-hidden p-8">
        <div
          class="pointer-events-none absolute -right-8 -top-8 h-32 w-32 rounded-full bg-teal-500/20 blur-2xl"
        />
        <p class="text-sm font-medium text-slate-400">Số dư khả dụng</p>
        <p class="mt-2 text-4xl font-bold tabular-nums text-white">
          {{ wallet.balance }}
          <span class="text-lg font-semibold text-slate-500">đ</span>
        </p>
        <dl class="mt-6 grid grid-cols-2 gap-4 text-sm">
          <div>
            <dt class="text-slate-500">Wallet ID</dt>
            <dd class="font-mono text-slate-200">{{ wallet.walletId }}</dd>
          </div>
          <div>
            <dt class="text-slate-500">User</dt>
            <dd class="text-slate-200">{{ wallet.userName }}</dd>
          </div>
        </dl>
      </div>

      <div class="ew-card p-6">
        <h2 class="text-lg font-semibold text-white">Nạp / Rút</h2>
        <p class="mt-1 text-xs text-slate-500">Gọi API gateway (idempotency mỗi lần bấm)</p>

        <div class="mt-4 space-y-4">
          <div class="flex flex-wrap gap-2">
            <input
              v-model="depositAmount"
              type="text"
              inputmode="decimal"
              placeholder="Số tiền nạp"
              class="ew-input max-w-[220px]"
            />
            <button type="button" class="ew-btn" :disabled="fundLoading || !depositAmount" @click="deposit">
              Nạp
            </button>
          </div>
          <div class="flex flex-wrap gap-2">
            <input
              v-model="withdrawAmount"
              type="text"
              inputmode="decimal"
              placeholder="Số tiền rút"
              class="ew-input max-w-[220px]"
            />
            <button
              type="button"
              class="ew-btn bg-gradient-to-r from-slate-600 to-slate-700 shadow-none hover:brightness-110"
              :disabled="fundLoading || !withdrawAmount"
              @click="withdraw"
            >
              Rút
            </button>
          </div>
          <p v-if="fundMsg" class="text-sm text-slate-300">{{ fundMsg }}</p>
        </div>
      </div>
    </div>

    <div class="mt-8 flex flex-wrap gap-4">
      <NuxtLink to="/transfer" class="ew-btn">Chuyển tiền</NuxtLink>
      <NuxtLink
        to="/history"
        class="rounded-xl border border-white/15 px-5 py-2.5 font-semibold text-slate-200 hover:bg-white/5"
      >
        Xem giao dịch gần đây
      </NuxtLink>
    </div>
  </div>
</template>
