import type { RecentRequestEntry } from '~/types/api'

const KEY_PREFIX = 'ewallet_recent_requests'
const LEGACY_KEY = 'ewallet_recent_requests'
const MAX = 30

/**
 * Lưu requestId gần đây theo user (localStorage key có userId).
 * Trước đây dùng một key chung → mọi tài khoản trên cùng trình duyệt thấy chung danh sách.
 */
export function useRecentRequests() {
  const { userId } = useAuth()

  function storageKey(): string | null {
    const uid = userId.value
    if (uid == null) return null
    return `${KEY_PREFIX}:${uid}`
  }

  function load(): RecentRequestEntry[] {
    if (!import.meta.client) return []
    const k = storageKey()
    if (!k) return []
    try {
      const raw = localStorage.getItem(k)
      if (!raw) {
        return migrateLegacyIfEmpty(k)
      }
      const arr = JSON.parse(raw) as RecentRequestEntry[]
      return Array.isArray(arr) ? arr : []
    } catch {
      return []
    }
  }

  /** Một lần: copy dữ liệu từ key cũ (không user) sang key theo user */
  function migrateLegacyIfEmpty(userKey: string): RecentRequestEntry[] {
    try {
      const legacy = localStorage.getItem(LEGACY_KEY)
      if (!legacy) return []
      const arr = JSON.parse(legacy) as RecentRequestEntry[]
      if (!Array.isArray(arr) || arr.length === 0) return []
      localStorage.setItem(userKey, JSON.stringify(arr.slice(0, MAX)))
      localStorage.removeItem(LEGACY_KEY)
      return arr
    } catch {
      return []
    }
  }

  function save(entries: RecentRequestEntry[]) {
    const k = storageKey()
    if (!k || !import.meta.client) return
    localStorage.setItem(k, JSON.stringify(entries.slice(0, MAX)))
  }

  function add(requestId: string) {
    const k = storageKey()
    if (!k) return
    const entries = load().filter((e) => e.requestId !== requestId)
    entries.unshift({
      requestId,
      createdAt: new Date().toISOString(),
    })
    save(entries)
  }

  return { load, add }
}
