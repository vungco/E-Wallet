<script setup lang="ts">
import type {
  AcceptedTransferResponse,
  CreateTransferRequest,
  TransferResultPayload,
  UserLookupResponse,
  WalletResponse,
} from '~/types/api'

definePageMeta({ middleware: 'auth' })

const { apiFetch } = useApi()
const { userEmail, setUserId } = useAuth()
const { messages, connect, subscribe } = useTransferSocket()
const { add: addRecent } = useRecentRequests()
const { markTransferPending } = useTransferAlerts()

const wallet = ref<WalletResponse | null>(null)
const requestId = ref('')
const recipientEmail = ref('')
const amount = ref('')
const error = ref('')
const submitting = ref(false)
const accepted = ref<AcceptedTransferResponse | null>(null)
const liveResult = ref<TransferResultPayload | null>(null)

function newRequestId() {
  requestId.value = crypto.randomUUID()
}

let unsubWs: (() => void) | undefined

onMounted(async () => {
  newRequestId()
  try {
    const w = await apiFetch<WalletResponse>('/api/v1/wallets')
    wallet.value = w
    setUserId(w.userId)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Không tải được ví'
  }
  connect()
  unsubWs = subscribe((p) => {
    if (p.requestId === requestId.value) {
      liveResult.value = p
    }
  })
})

onUnmounted(() => {
  unsubWs?.()
})

watch(requestId, () => {
  liveResult.value = null
  accepted.value = null
})

async function submit() {
  error.value = ''
  liveResult.value = null
  accepted.value = null
  if (!wallet.value) {
    error.value = 'Chưa có thông tin ví'
    return
  }
  const amt = Number(amount.value.replace(',', '.'))
  if (!Number.isFinite(amt) || amt <= 0) {
    error.value = 'Số tiền không hợp lệ'
    return
  }
  submitting.value = true
  try {
    const toE = recipientEmail.value.trim()
    if (!toE) {
      error.value = 'Nhập email người nhận'
      return
    }
    let resolved: UserLookupResponse
    try {
      resolved = await apiFetch<UserLookupResponse>(
        `/api/v1/users/lookup?email=${encodeURIComponent(toE)}`
      )
    } catch (e) {
      error.value =
        e instanceof Error ? e.message : 'Không tìm thấy user theo email (hoặc không thể chuyển cho chính bạn)'
      return
    }
    const body: CreateTransferRequest = {
      requestId: requestId.value,
      fromWalletId: wallet.value.walletId,
      toWalletId: resolved.walletId,
      toUserId: resolved.userId,
      amount: amt,
    }
    const fromE = userEmail.value?.trim()
    if (fromE) body.fromUserEmail = fromE
    body.toUserEmail = resolved.email
    const res = await apiFetch<AcceptedTransferResponse>('/api/v1/transfers', {
      method: 'POST',
      body: JSON.stringify(body),
    })
    accepted.value = res
    markTransferPending(res.requestId)
    addRecent(requestId.value)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Không tạo được lệnh'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-2xl px-4 py-10">
    <h1 class="text-3xl font-bold text-white">Chuyển tiền</h1>
    <p class="mt-1 text-slate-400">
      HTTP <span class="font-mono text-teal-300/90">202 ACCEPTED</span> — kết quả qua WebSocket (hoặc xem
      <NuxtLink to="/history" class="text-teal-400 underline">lịch sử</NuxtLink>)
    </p>
    <p class="mt-2 text-sm text-slate-500">
      Sau khi gửi lệnh, bạn có thể rời trang này: khi lệnh xử lý xong, hệ thống sẽ
      <strong class="text-slate-400">bật thông báo góc màn hình</strong> (thành công / thất bại). Trên header
      hiện <span class="text-amber-200/90">Chờ xử lý</span> nếu còn lệnh chưa có kết quả.
    </p>

    <div v-if="!wallet && !error" class="mt-8 text-slate-400">Đang tải ví…</div>
    <p v-else-if="error && !wallet" class="mt-4 text-red-300">{{ error }}</p>

    <form v-else class="ew-card mt-8 space-y-5 p-8" @submit.prevent="submit">
      <div class="flex flex-wrap items-end justify-between gap-2">
        <div class="min-w-0 flex-1">
          <label class="ew-label">requestId (idempotency)</label>
          <p class="break-all font-mono text-xs text-slate-500">{{ requestId }}</p>
        </div>
        <button type="button" class="ew-btn-ghost text-sm" @click="newRequestId">Sinh mới</button>
      </div>

      <div>
        <label class="ew-label">Ví nguồn</label>
        <input class="ew-input opacity-80" type="text" :value="wallet?.walletId" readonly />
      </div>

      <div>
        <label class="ew-label" for="toMail">Email người nhận</label>
        <input
          id="toMail"
          v-model="recipientEmail"
          class="ew-input"
          type="email"
          autocomplete="email"
          required
          placeholder="user@example.com"
        />
        <p class="mt-1 text-xs text-slate-500">
          Hệ thống tra user theo email rồi gửi lệnh chuyển (không cần nhập ID ví). Không thể chuyển cho chính bạn.
        </p>
      </div>

      <div>
        <label class="ew-label" for="amt">Số tiền</label>
        <input
          id="amt"
          v-model="amount"
          class="ew-input"
          type="text"
          inputmode="decimal"
          required
          placeholder="0.0001"
        />
      </div>

      <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-300">{{ error }}</p>

      <button type="submit" class="ew-btn w-full" :disabled="submitting">
        {{ submitting ? 'Đang gửi…' : 'Gửi lệnh chuyển' }}
      </button>
    </form>

    <div v-if="accepted" class="ew-card mt-6 border-teal-500/20 p-6">
      <p class="text-sm font-medium text-teal-200">Đã chấp nhận lệnh</p>
      <p class="mt-1 font-mono text-sm text-slate-300">
        requestId: {{ accepted.requestId }} · {{ accepted.status }}
      </p>
    </div>

    <div v-if="liveResult" class="ew-card mt-6 border-emerald-500/30 p-6">
      <p class="text-sm font-semibold text-emerald-300">Kết quả (WebSocket)</p>
      <dl class="mt-3 grid gap-2 text-sm text-slate-300">
        <div class="flex justify-between gap-4">
          <dt class="text-slate-500">Trạng thái</dt>
          <dd class="font-medium text-white">{{ liveResult.status }}</dd>
        </div>
        <div v-if="liveResult.transactionId != null" class="flex justify-between gap-4">
          <dt class="text-slate-500">transactionId</dt>
          <dd class="font-mono">{{ liveResult.transactionId }}</dd>
        </div>
        <div v-if="liveResult.errorMessage" class="flex justify-between gap-4">
          <dt class="text-slate-500">Lỗi</dt>
          <dd class="text-red-300">{{ liveResult.errorMessage }}</dd>
        </div>
      </dl>
    </div>

    <div v-if="messages.length" class="mt-8">
      <h2 class="text-sm font-semibold uppercase tracking-wider text-slate-500">Gói WS gần đây</h2>
      <ul class="mt-2 space-y-2 font-mono text-xs text-slate-500">
        <li v-for="(m, i) in messages.slice(0, 5)" :key="i">
          {{ m.requestId?.slice(0, 8) }}… · {{ m.status }}
        </li>
      </ul>
    </div>
  </div>
</template>
