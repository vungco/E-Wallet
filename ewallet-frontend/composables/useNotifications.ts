import type { NotificationItem, NotificationPageResponse, UnreadCountResponse } from '~/types/api'

/** WS tới trước khi notification-worker kịp ghi DB — gọi lại sau vài nhịp. */
const REFRESH_DELAYS_MS = [0, 400, 1200, 2800]

export function useNotifications() {
  const unreadCount = useState('notifications.unreadCount', () => 0)

  async function refreshUnreadCount() {
    const { apiFetch } = useApi()
    try {
      const r = await apiFetch<UnreadCountResponse>('/api/v1/notifications/unread-count')
      unreadCount.value = Number(r.count)
    } catch {
      /* ignore */
    }
  }

  /**
   * Sau sự kiện real-time (vd. WS chuyển tiền), đồng bộ badge với server nhiều lần
   * để bắt kịp khi Kafka/DB xử lý trễ.
   */
  function scheduleRefreshUnreadCount() {
    if (import.meta.dev) {
      console.info(
        '[notif-badge] staggered unread refresh:',
        REFRESH_DELAYS_MS,
        'ms — WS tới trước DB/Kafka; badge bắt kịp sau vài nhịp',
      )
    }
    for (const delay of REFRESH_DELAYS_MS) {
      if (delay === 0) {
        void refreshUnreadCount()
      } else {
        setTimeout(() => {
          void refreshUnreadCount()
        }, delay)
      }
    }
  }

  async function fetchPage(
    filter: 'unread' | 'read',
    cursor?: string,
    limit = 20,
  ): Promise<NotificationPageResponse> {
    const { apiFetch } = useApi()
    const qs = new URLSearchParams({ filter })
    if (cursor) qs.set('cursor', cursor)
    qs.set('limit', String(limit))
    return apiFetch<NotificationPageResponse>(`/api/v1/notifications?${qs}`)
  }

  async function fetchOne(id: number, markRead = true): Promise<NotificationItem> {
    const { apiFetch } = useApi()
    const q = markRead ? '?markRead=true' : ''
    return apiFetch<NotificationItem>(`/api/v1/notifications/${id}${q}`)
  }

  async function markAsRead(id: number): Promise<void> {
    const { apiFetch } = useApi()
    await apiFetch(`/api/v1/notifications/${id}/read`, { method: 'POST' })
  }

  return {
    unreadCount,
    refreshUnreadCount,
    scheduleRefreshUnreadCount,
    fetchPage,
    fetchOne,
    markAsRead,
  }
}
