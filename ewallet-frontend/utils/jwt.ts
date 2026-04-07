/** Decode JWT payload (không verify chữ ký — chỉ đọc sub/userId cho UI). */
export function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = b64.length % 4 === 0 ? '' : '='.repeat(4 - (b64.length % 4))
    const json = atob(b64 + pad)
    return JSON.parse(json) as Record<string, unknown>
  } catch {
    return null
  }
}

export function jwtSubjectUserId(token: string | null | undefined): number | null {
  if (!token) return null
  const p = decodeJwtPayload(token)
  if (!p) return null
  const sub = p.sub
  if (typeof sub === 'string' || typeof sub === 'number') {
    const n = Number(sub)
    return Number.isFinite(n) ? n : null
  }
  return null
}
