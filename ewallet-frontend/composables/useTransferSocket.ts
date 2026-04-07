import type { TransferResultPayload } from '~/types/api'

function httpToWsUrl(base: string): string {
  return base
    .trim()
    .replace(/^https:\/\//i, 'wss://')
    .replace(/^http:\/\//i, 'ws://')
}

let clientSocket: WebSocket | null = null

/**
 * WebSocket tới ws-gateway: {@code /ws?token=<JWT>} — cùng access token REST.
 */
export function useTransferSocket() {
  const config = useRuntimeConfig()
  const { accessToken } = useAuth()

  const status = useState<'idle' | 'connecting' | 'open' | 'closed' | 'error'>(
    'ws.status',
    () => 'idle',
  )
  const messages = useState<TransferResultPayload[]>('ws.messages', () => [])

  const listeners = new Set<(p: TransferResultPayload) => void>()

  function subscribe(fn: (p: TransferResultPayload) => void) {
    listeners.add(fn)
    return () => listeners.delete(fn)
  }

  function pushMessage(p: TransferResultPayload) {
    const arr = [...messages.value]
    arr.unshift(p)
    messages.value = arr.slice(0, 80)
    listeners.forEach((fn) => {
      try {
        fn(p)
      } catch {
        /* ignore */
      }
    })
  }

  function connect() {
    if (!import.meta.client) return
    const token = accessToken.value
    if (!token) return

    if (clientSocket?.readyState === WebSocket.OPEN) return
    if (clientSocket?.readyState === WebSocket.CONNECTING) return

    status.value = 'connecting'
    const wsRoot = httpToWsUrl(String(config.public.wsBase))
    const url = `${wsRoot.replace(/\/$/, '')}/ws?token=${encodeURIComponent(token)}`

    const ws = new WebSocket(url)
    clientSocket = ws

    ws.onopen = () => {
      status.value = 'open'
    }
    ws.onmessage = (ev) => {
      try {
        const p = JSON.parse(String(ev.data)) as TransferResultPayload
        if (p?.requestId && p?.status) {
          if (import.meta.dev) {
            console.info('[transfer-ws] message', {
              requestId: p.requestId,
              status: p.status,
              at: new Date().toISOString(),
            })
          }
          pushMessage(p)
        }
      } catch {
        /* ignore non-JSON */
      }
    }
    ws.onerror = () => {
      status.value = 'error'
    }
    ws.onclose = () => {
      if (clientSocket === ws) clientSocket = null
      status.value = 'closed'
    }
  }

  function disconnect() {
    if (!import.meta.client) return
    const ws = clientSocket
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
      ws.close()
    }
    clientSocket = null
    status.value = 'idle'
  }

  /** Đóng và mở lại với access token hiện tại (sau refresh JWT). */
  function reconnect() {
    if (!import.meta.client) return
    disconnect()
    connect()
  }

  return {
    status,
    messages,
    connect,
    disconnect,
    reconnect,
    subscribe,
  }
}
