<script setup lang="ts">
/**
 * Gom chuông thông báo + badge chờ WS + đăng xuất để layout không subscribe
 * useTransferAlerts (pending) — tránh re-render cả header khi có kết quả chuyển tiền.
 */
const { pendingCount, clearAllPending } = useTransferAlerts()
const { logout, refreshToken } = useAuth()

async function onLogout() {
  const { apiFetch } = useApi()
  try {
    if (refreshToken.value) {
      await apiFetch('/api/v1/auth/logout', {
        method: 'POST',
        body: JSON.stringify({ refreshToken: refreshToken.value }),
      })
    }
  } catch {
    /* ignore */
  }
  useTransferSocket().disconnect()
  clearAllPending()
  logout()
  await navigateTo('/login')
}
</script>

<template>
  <div class="flex items-center gap-2">
    <NotificationBell />
    <span
      v-if="pendingCount > 0"
      class="hidden rounded-full border border-amber-500/40 bg-amber-500/10 px-2.5 py-1 text-xs font-medium text-amber-200 sm:inline"
      title="Đang chờ kết quả chuyển tiền từ server"
    >
      Chờ xử lý: {{ pendingCount }}
    </span>
    <button type="button" class="ew-btn-ghost text-sm" @click="onLogout">Đăng xuất</button>
  </div>
</template>
