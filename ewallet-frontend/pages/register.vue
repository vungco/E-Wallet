<script setup lang="ts">
import type { RegisterRequest, RegisterResponse } from '~/types/api'

definePageMeta({ layout: 'default' })

const { apiFetch } = useApi()
const { isLoggedIn } = useAuth()

const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const done = ref<RegisterResponse | null>(null)

watchEffect(() => {
  if (isLoggedIn.value) navigateTo('/')
})

async function submit() {
  error.value = ''
  loading.value = true
  done.value = null
  try {
    const body: RegisterRequest = {
      name: name.value.trim(),
      email: email.value.trim(),
      password: password.value,
    }
    const res = await apiFetch<RegisterResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(body),
    })
    done.value = res
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Đăng ký thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-md px-4 py-16">
    <div class="ew-card p-8">
      <h1 class="text-2xl font-bold text-white">Đăng ký</h1>
      <p class="mt-1 text-sm text-slate-400">Tạo ví điện tử — một user một ví</p>

      <div v-if="done" class="mt-6 rounded-xl border border-teal-500/30 bg-teal-500/10 p-4 text-sm text-teal-100">
        <p class="font-medium">{{ done.message }}</p>
        <p class="mt-2 text-slate-300">User ID: {{ done.userId }}</p>
        <NuxtLink to="/login" class="mt-4 inline-block font-semibold text-teal-300 hover:underline">
          Đăng nhập ngay →
        </NuxtLink>
      </div>

      <form v-else class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <label class="ew-label" for="name">Họ tên</label>
          <input id="name" v-model="name" type="text" class="ew-input" required maxlength="255" />
        </div>
        <div>
          <label class="ew-label" for="email">Email</label>
          <input id="email" v-model="email" type="email" class="ew-input" required />
        </div>
        <div>
          <label class="ew-label" for="password">Mật khẩu (≥ 8 ký tự)</label>
          <input
            id="password"
            v-model="password"
            type="password"
            class="ew-input"
            required
            minlength="8"
            maxlength="128"
          />
        </div>

        <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-300">
          {{ error }}
        </p>

        <button type="submit" class="ew-btn w-full" :disabled="loading">
          {{ loading ? 'Đang tạo tài khoản…' : 'Đăng ký' }}
        </button>
      </form>

      <p class="mt-6 text-center text-sm text-slate-500">
        Đã có tài khoản?
        <NuxtLink to="/login" class="font-medium text-teal-400 hover:underline">Đăng nhập</NuxtLink>
      </p>
    </div>
  </div>
</template>
