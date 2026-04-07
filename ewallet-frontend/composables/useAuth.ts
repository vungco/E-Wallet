const STORAGE_ACCESS = 'ewallet_access_token'
const STORAGE_REFRESH = 'ewallet_refresh_token'
const STORAGE_EMAIL = 'ewallet_user_email'
const STORAGE_USER_ID = 'ewallet_user_id'

export function useAuth() {
  const accessToken = useState<string | null>('auth.access', () => null)
  const refreshToken = useState<string | null>('auth.refresh', () => null)
  /** Email đăng nhập — gửi kèm CreateTransfer để notification không phải tra DB */
  const userEmail = useState<string | null>('auth.email', () => null)
  /** Từ GET /api/v1/wallets — dùng scope localStorage (vd. giao dịch gần đây) */
  const userId = useState<number | null>('auth.userId', () => null)

  const isLoggedIn = computed(() => !!accessToken.value?.length)

  function setUserEmail(email: string | null) {
    userEmail.value = email
    if (import.meta.client) {
      if (email) localStorage.setItem(STORAGE_EMAIL, email)
      else localStorage.removeItem(STORAGE_EMAIL)
    }
  }

  function setTokens(access: string | null, refresh: string | null) {
    accessToken.value = access
    refreshToken.value = refresh
    if (import.meta.client) {
      if (access) localStorage.setItem(STORAGE_ACCESS, access)
      else localStorage.removeItem(STORAGE_ACCESS)
      if (refresh) localStorage.setItem(STORAGE_REFRESH, refresh)
      else localStorage.removeItem(STORAGE_REFRESH)
    }
  }

  function setUserId(id: number | null) {
    userId.value = id
    if (import.meta.client) {
      if (id != null) localStorage.setItem(STORAGE_USER_ID, String(id))
      else localStorage.removeItem(STORAGE_USER_ID)
    }
  }

  function hydrateFromStorage() {
    if (!import.meta.client) return
    const a = localStorage.getItem(STORAGE_ACCESS)
    const r = localStorage.getItem(STORAGE_REFRESH)
    const e = localStorage.getItem(STORAGE_EMAIL)
    const uid = localStorage.getItem(STORAGE_USER_ID)
    if (a) accessToken.value = a
    if (r) refreshToken.value = r
    if (e) userEmail.value = e
    if (uid) {
      const n = Number(uid)
      if (Number.isFinite(n)) userId.value = n
    }
  }

  function logout() {
    setTokens(null, null)
    setUserEmail(null)
    setUserId(null)
  }

  return {
    accessToken,
    refreshToken,
    userEmail,
    userId,
    isLoggedIn,
    setTokens,
    setUserEmail,
    setUserId,
    hydrateFromStorage,
    logout,
  }
}
