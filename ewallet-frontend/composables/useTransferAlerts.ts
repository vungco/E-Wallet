import type { TransferResultPayload } from '~/types/api'
import { jwtSubjectUserId } from '~/utils/jwt'

const STORAGE_PENDING = 'ewallet_pending_transfer_ids'
const STORAGE_SEEN = 'ewallet_ws_seen_keys'

export interface TransferToast {
  id: string
  title: string
  body: string
  variant: 'success' | 'error' | 'info'
  at: number
}

function loadSeenKeys(): Set<string> {
  if (!import.meta.client) return new Set()
  try {
    const raw = sessionStorage.getItem(STORAGE_SEEN)
    if (!raw) return new Set()
    const arr = JSON.parse(raw) as string[]
    return new Set(Array.isArray(arr) ? arr : [])
  } catch {
    return new Set()
  }
}

function saveSeenKeys(keys: Set<string>) {
  if (!import.meta.client) return
  sessionStorage.setItem(STORAGE_SEEN, JSON.stringify([...keys].slice(-200)))
}

function loadPendingIds(): Set<string> {
  if (!import.meta.client) return new Set()
  try {
    const raw = localStorage.getItem(STORAGE_PENDING)
    if (!raw) return new Set()
    const arr = JSON.parse(raw) as string[]
    return new Set(Array.isArray(arr) ? arr : [])
  } catch {
    return new Set()
  }
}

function savePendingIds(ids: Set<string>) {
  if (!import.meta.client) return
  localStorage.setItem(STORAGE_PENDING, JSON.stringify([...ids].slice(-50)))
}

/**
 * Toast + theo dõi lệnh đã gửi (202): khi WS báo SUCCESS/FAILED thì thông báo và gỡ khỏi hàng chờ.
 */
export function useTransferAlerts() {
  const { accessToken } = useAuth()

  const toasts = useState<TransferToast[]>('transferAlerts.toasts', () => [])
  const pendingIds = useState<Set<string>>('transferAlerts.pending', () => new Set())

  function syncPendingFromStorage() {
    pendingIds.value = loadPendingIds()
  }

  function markTransferPending(requestId: string) {
    const next = new Set(pendingIds.value)
    next.add(requestId)
    pendingIds.value = next
    savePendingIds(next)
  }

  function clearPending(requestId: string) {
    const next = new Set(pendingIds.value)
    next.delete(requestId)
    pendingIds.value = next
    savePendingIds(next)
  }

  /** Gọi khi đăng xuất — xóa hàng chờ “chờ xử lý” trên máy này. */
  function clearAllPending() {
    pendingIds.value = new Set()
    if (import.meta.client) localStorage.removeItem(STORAGE_PENDING)
  }

  const pendingCount = computed(() => pendingIds.value.size)

  function dismiss(id: string) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  function pushToast(t: Omit<TransferToast, 'at'> & { at?: number }) {
    const item: TransferToast = {
      ...t,
      at: t.at ?? Date.now(),
    }
    toasts.value = [item, ...toasts.value].slice(0, 6)
    const ms = t.variant === 'error' ? 14000 : 10000
    if (import.meta.client) {
      setTimeout(() => dismiss(item.id), ms)
    }
  }

  function handleWsPayload(p: TransferResultPayload) {
    if (!p?.requestId || !p?.status) return
    const myId = jwtSubjectUserId(accessToken.value)
    if (myId == null || p.userId !== myId) return

    const dedupeKey = `${p.requestId}:${p.status}`
    const seen = loadSeenKeys()
    if (seen.has(dedupeKey)) return
    seen.add(dedupeKey)
    saveSeenKeys(seen)

    const wasPending = pendingIds.value.has(p.requestId)
    if (wasPending) clearPending(p.requestId)

    const ok = p.status === 'SUCCESS'
    const title = ok
      ? 'Chuyển tiền thành công'
      : p.status === 'FAILED'
        ? 'Chuyển tiền thất bại'
        : `Trạng thái: ${p.status}`

    const lines = [
      `Mã lệnh: ${p.requestId}`,
      p.transactionId != null ? `Giao dịch: #${p.transactionId}` : null,
      p.errorMessage ? `Chi tiết: ${p.errorMessage}` : null,
      wasPending ? null : 'Liên quan ví của bạn (ví dụ: nhận tiền).',
    ].filter(Boolean) as string[]

    pushToast({
      id: dedupeKey,
      title,
      body: lines.join('\n'),
      variant: ok ? 'success' : 'error',
    })
  }

  return {
    toasts,
    pendingCount,
    markTransferPending,
    clearPending,
    clearAllPending,
    dismiss,
    handleWsPayload,
    syncPendingFromStorage,
  }
}
