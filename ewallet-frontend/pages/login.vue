<script setup lang="ts">
import type { LoginRequest, TokenResponse } from '~/types/api'

definePageMeta({ layout: 'default' })

const { apiFetch } = useApi()
const { setTokens, setUserEmail, isLoggedIn } = useAuth()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

watchEffect(() => {
  if (isLoggedIn.value) navigateTo('/')
})

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const body: LoginRequest = { email: email.value.trim(), password: password.value }
    const res = await apiFetch<TokenResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(body),
    })
    setTokens(res.accessToken, res.refreshToken)
    setUserEmail(body.email)
    await navigateTo('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Đăng nhập thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-md px-4 py-16">
    <div class="ew-card p-8">
      <h1 class="text-2xl font-bold text-white">Đăng nhập</h1>
      <p class="mt-1 text-sm text-slate-400">Tiếp tục tới ví của bạn</p>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <label class="ew-label" for="email">Email</label>
          <input id="email" v-model="email" type="email" class="ew-input" required autocomplete="email" />
        </div>
        <div>
          <label class="ew-label" for="password">Mật khẩu</label>
          <input
            id="password"
            v-model="password"
            type="password"
            class="ew-input"
            required
            autocomplete="current-password"
          />
        </div>

        <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-300">
          {{ error }}
        </p>

        <button type="submit" class="ew-btn w-full" :disabled="loading">
          {{ loading ? 'Đang xử lý…' : 'Đăng nhập' }}
        </button>
      </form>

      <p class="mt-6 text-center text-sm text-slate-500">
        Chưa có tài khoản?
        <NuxtLink to="/register" class="font-medium text-teal-400 hover:underline">Đăng ký</NuxtLink>
      </p>
    </div>
  </div>
</template>
