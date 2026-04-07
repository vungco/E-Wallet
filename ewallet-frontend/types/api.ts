/** Khớp ewallet-gateway DTO (JSON) */

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export interface RegisterResponse {
  userId: number
  email: string
  name: string
  message: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface WalletResponse {
  walletId: number
  userId: number
  userName: string
  balance: string
  version: number
}

/** GET /api/v1/users/lookup?email= — tra user + ví theo email (JWT) */
export interface UserLookupResponse {
  userId: number
  walletId: number
  email: string
  name: string
}

export interface WalletFundRequest {
  amount: number
  idempotencyKey: string
}

export interface WalletOperationResponse {
  walletId: number
  balanceAfter: string
  version: number
  replayed: boolean
}

export interface CreateTransferRequest {
  requestId: string
  fromWalletId: number
  toWalletId: number
  toUserId: number
  /** JSON number — khớp BigDecimal phía gateway */
  amount: number
  /** Tuỳ chọn: đưa xuống Kafka để gửi mail không query lại users */
  fromUserEmail?: string
  toUserEmail?: string
}

export interface AcceptedTransferResponse {
  requestId: string
  status: string
}

export interface TransferStatusResponse {
  requestId: string
  status: string
  fromWalletId: number
  toWalletId: number
  amount: string
  errorMessage: string | null
}

/** ws-gateway — JSON push (Redis / Kafka) */
export interface TransferResultPayload {
  userId: number
  requestId: string
  status: string
  transactionId: number | null
  errorMessage: string | null
}

export interface ApiErrorBody {
  code?: string
  message?: string
}

export interface RecentRequestEntry {
  requestId: string
  createdAt: string
}

/** ewallet-gateway — thông báo (notification-worker qua gRPC) */
export interface NotificationItem {
  id: number
  title: string
  body: string
  transferStatus: string
  read: boolean
  requestId: string
  transactionId: number | null
  amount: string | null
  counterpartUserId: number | null
  userRole: string | null
  createdAt: string | null
}

export interface NotificationPageResponse {
  items: NotificationItem[]
  nextCursor: string
  hasMore: boolean
}

export interface UnreadCountResponse {
  count: number
}
