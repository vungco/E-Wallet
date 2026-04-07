import type { ApiErrorBody, TokenResponse } from '~/types/api'

/** Một lần refresh cho nhiều request 401 đồng thời */
let refreshPromise: Promise<boolean> | null = null

function shouldSkipRefreshOn401(path: string): boolean {
  const p = path.startsWith('/') ? path : `/${path}`
  return (
    p.includes('/api/v1/auth/refresh') ||
    p.includes('/api/v1/auth/login') ||
    p.includes('/api/v1/auth/register')
  )
}

/**
 * Gọi POST /api/v1/auth/refresh — không qua apiFetch để tránh đệ quy.
 */
async function tryRefreshTokens(): Promise<boolean> {
  if (!import.meta.client) return false
  if (refreshPromise) return refreshPromise

  refreshPromise = (async (): Promise<boolean> => {
    const { refreshToken, setTokens, logout } = useAuth()
    const rt = refreshToken.value
    if (!rt) {
      logout()
      return false
    }

    const config = useRuntimeConfig()
    const base = String(config.public.apiBase).replace(/\/$/, '')
    const url = `${base}/api/v1/auth/refresh`

    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: rt }),
      })
      const text = await res.text()
      if (!res.ok) {
        logout()
        return false
      }
      const data = text ? (JSON.parse(text) as TokenResponse) : null
      if (!data?.accessToken) {
        logout()
        return false
      }
      setTokens(data.accessToken, data.refreshToken ?? rt)
      try {
        useTransferSocket().reconnect()
      } catch {
        /* ignore */
      }
      return true
    } catch {
      logout()
      return false
    } finally {
      refreshPromise = null
    }
  })()

  return refreshPromise
}

export function useApi() {
  const config = useRuntimeConfig()
  const { accessToken } = useAuth()

  const base = () => config.public.apiBase.replace(/\/$/, '')

  async function apiFetch<T>(path: string, opts: RequestInit = {}): Promise<T> {
    const run = async (): Promise<Response> => {
      const headers = new Headers(opts.headers)
      if (!headers.has('Content-Type') && opts.body != null) {
        headers.set('Content-Type', 'application/json')
      }
      const token = accessToken.value
      if (token) headers.set('Authorization', `Bearer ${token}`)

      const url = `${base()}${path.startsWith('/') ? path : `/${path}`}`
      return fetch(url, { ...opts, headers })
    }

    let res = await run()

    if (res.status === 401 && import.meta.client && !shouldSkipRefreshOn401(path)) {
      const refreshed = await tryRefreshTokens()
      if (refreshed) {
        res = await run()
      }
    }

    if (res.status === 204) {
      return undefined as T
    }

    const text = await res.text()
    const data = text ? (JSON.parse(text) as unknown) : null

    if (!res.ok) {
      const err = data as ApiErrorBody | null
      const msg =
        err?.message ||
        (typeof data === 'object' && data !== null && 'message' in data
          ? String((data as { message?: string }).message)
          : null) ||
        res.statusText
      throw new Error(msg || `HTTP ${res.status}`)
    }

    return data as T
  }

  return { apiFetch, apiBase: base }
}
