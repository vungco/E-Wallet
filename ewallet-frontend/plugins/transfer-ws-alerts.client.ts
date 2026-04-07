/**
 * Kết nối WS sau khi có token + đăng ký toast toàn cục cho mọi kết quả chuyển tiền (SUCCESS/FAILED).
 */
export default defineNuxtPlugin(() => {
  const { accessToken } = useAuth()
  const { connect, subscribe } = useTransferSocket()
  const alerts = useTransferAlerts()
  const notifications = useNotifications()

  alerts.syncPendingFromStorage()

  let unsub: (() => void) | undefined

  watch(
    accessToken,
    (token) => {
      unsub?.()
      unsub = undefined
      if (!token) return
      void notifications.refreshUnreadCount()
      connect()
      unsub = subscribe((p) => {
        alerts.handleWsPayload(p)
        notifications.scheduleRefreshUnreadCount()
      })
    },
    { immediate: true },
  )
})
