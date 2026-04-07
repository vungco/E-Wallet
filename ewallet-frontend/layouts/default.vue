<script setup lang="ts">
const { isLoggedIn } = useAuth()
const route = useRoute()

const nav = [
  { to: '/', label: 'Tổng quan' },
  { to: '/transfer', label: 'Chuyển tiền' },
  { to: '/history', label: 'Giao dịch' },
]
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <header
      class="sticky top-0 z-50 border-b border-white/5 bg-slate-950/80 backdrop-blur-md"
    >
      <div class="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4">
        <NuxtLink to="/" class="group flex items-center gap-2">
          <span
            class="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-teal-400 to-emerald-600 text-lg font-bold text-slate-950 shadow-glow"
            >E</span
          >
          <span
            class="text-lg font-semibold tracking-tight text-white group-hover:text-teal-300 transition"
            >Mini E-Wallet</span
          >
        </NuxtLink>

        <nav v-if="isLoggedIn" class="flex flex-wrap items-center justify-end gap-1">
          <NuxtLink
            v-for="item in nav"
            :key="item.to"
            :to="item.to"
            class="rounded-lg px-3 py-2 text-sm font-medium transition"
            :class="
              route.path === item.to
                ? 'bg-white/10 text-white'
                : 'text-slate-400 hover:bg-white/5 hover:text-white'
            "
          >
            {{ item.label }}
          </NuxtLink>
        </nav>

        <div class="flex items-center gap-2">
          <template v-if="isLoggedIn">
            <HeaderUserActions />
          </template>
          <template v-else>
            <NuxtLink to="/login" class="ew-btn-ghost text-sm">Đăng nhập</NuxtLink>
            <NuxtLink
              to="/register"
              class="rounded-xl bg-white/10 px-4 py-2 text-sm font-semibold text-white hover:bg-white/15"
            >
              Đăng ký
            </NuxtLink>
          </template>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <slot />
    </main>

    <footer class="border-t border-white/5 py-6 text-center text-xs text-slate-600">
      Mini E-Wallet · dev
    </footer>

    <TransferToastStack />
  </div>
</template>
