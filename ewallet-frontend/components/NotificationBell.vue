<script setup lang="ts">
import type { NotificationItem } from '~/types/api'

type NotifTab = 'unread' | 'read'

const { unreadCount, refreshUnreadCount, fetchPage } = useNotifications()
const open = ref(false)
const loading = ref(false)
const tab = ref<NotifTab>('unread')
const unreadPreview = ref<NotificationItem[]>([])
const readPreview = ref<NotificationItem[]>([])
const readLoaded = ref(false)

const buttonRef = ref<HTMLElement | null>(null)
const panelStyle = ref<Record<string, string>>({})

function updatePanelPosition() {
  const el = buttonRef.value
  if (!el) return
  const r = el.getBoundingClientRect()
  const gap = 8
  const vw = document.documentElement.clientWidth
  panelStyle.value = {
    top: `${r.bottom + gap}px`,
    right: `${vw - r.right}px`,
  }
}

async function loadUnreadOnly() {
  loading.value = true
  try {
    await refreshUnreadCount()
    const u = await fetchPage('unread', undefined, 10)
    unreadPreview.value = u.items
  } finally {
    loading.value = false
  }
}

async function ensureReadLoaded() {
  if (readLoaded.value) return
  loading.value = true
  try {
    const r = await fetchPage('read', undefined, 10)
    readPreview.value = r.items
    readLoaded.value = true
  } finally {
    loading.value = false
  }
}

function setTab(next: NotifTab) {
  tab.value = next
  if (next === 'read') void ensureReadLoaded()
}

function toggle() {
  open.value = !open.value
  if (open.value) {
    tab.value = 'unread'
    readLoaded.value = false
    readPreview.value = []
    nextTick(() => {
      updatePanelPosition()
      void loadUnreadOnly()
    })
  }
}

function close() {
  open.value = false
}

function formatWhen(iso: string | null) {
  if (!iso) return ''
  try {
    return new Date(iso).toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return iso
  }
}

const currentItems = computed(() => (tab.value === 'unread' ? unreadPreview.value : readPreview.value))

let removeListeners: (() => void) | undefined

watch(open, (isOpen) => {
  removeListeners?.()
  removeListeners = undefined
  if (!isOpen) return

  nextTick(() => updatePanelPosition())

  const onMove = () => updatePanelPosition()
  window.addEventListener('resize', onMove)
  window.addEventListener('scroll', onMove, true)
  removeListeners = () => {
    window.removeEventListener('resize', onMove)
    window.removeEventListener('scroll', onMove, true)
  }
})

onUnmounted(() => {
  removeListeners?.()
})
</script>

<template>
  <div class="relative">
    <button
      ref="buttonRef"
      type="button"
      class="relative flex h-10 w-10 items-center justify-center rounded-xl text-slate-300 transition hover:bg-white/10 hover:text-white"
      aria-label="Thông báo"
      :aria-expanded="open"
      @click.stop="toggle"
    >
      <svg
        class="h-5 w-5"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
        />
      </svg>
      <span
        v-if="unreadCount > 0"
        class="absolute -right-0.5 -top-0.5 flex min-h-[1.125rem] min-w-[1.125rem] items-center justify-center rounded-full bg-rose-500 px-1 text-[10px] font-bold leading-none text-white shadow"
      >
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <Teleport to="body">
      <Transition
        enter-active-class="transition ease-out duration-150"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition ease-in duration-100"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div
          v-if="open"
          class="fixed inset-0 z-[200] isolate"
          role="dialog"
          aria-modal="true"
          aria-label="Danh sách thông báo"
        >
          <div
            class="absolute inset-0 bg-black/40 sm:bg-black/20"
            aria-hidden="true"
            @click="close"
          />
          <div
            class="absolute z-10 w-[min(100vw-2rem,24rem)] origin-top-right rounded-2xl border border-white/10 bg-slate-900/95 shadow-2xl backdrop-blur-md"
            :style="panelStyle"
            @click.stop
          >
            <div class="border-b border-white/10 px-3 pt-3 pb-0">
              <div class="flex items-center justify-between gap-2 px-1 pb-2">
                <h2 class="text-sm font-semibold text-white">Thông báo</h2>
                <span v-if="unreadCount > 0" class="text-xs text-teal-300">
                  {{ unreadCount }} chưa đọc
                </span>
              </div>
              <div
                class="flex rounded-t-lg bg-white/[0.04] p-0.5"
                role="tablist"
                aria-label="Lọc thông báo"
              >
                <button
                  type="button"
                  role="tab"
                  :aria-selected="tab === 'unread'"
                  class="flex-1 rounded-md px-2 py-2 text-center text-xs font-semibold transition sm:text-sm"
                  :class="
                    tab === 'unread'
                      ? 'bg-white/10 text-white shadow-sm'
                      : 'text-slate-400 hover:text-slate-200'
                  "
                  @click="setTab('unread')"
                >
                  Chưa đọc
                </button>
                <button
                  type="button"
                  role="tab"
                  :aria-selected="tab === 'read'"
                  class="flex-1 rounded-md px-2 py-2 text-center text-xs font-semibold transition sm:text-sm"
                  :class="
                    tab === 'read'
                      ? 'bg-white/10 text-white shadow-sm'
                      : 'text-slate-400 hover:text-slate-200'
                  "
                  @click="setTab('read')"
                >
                  Đã đọc
                </button>
              </div>
            </div>

            <div class="max-h-[min(70vh,28rem)] overflow-y-auto px-3 py-3">
              <div v-if="loading" class="px-2 py-8 text-center text-sm text-slate-500">Đang tải…</div>
              <template v-else>
                <ul v-if="currentItems.length" class="space-y-1">
                  <li v-for="n in currentItems" :key="n.id">
                    <NuxtLink
                      :to="`/notifications/${n.id}`"
                      class="block rounded-xl px-2 py-2 transition hover:bg-white/5"
                      :class="tab === 'unread' ? '' : 'opacity-90'"
                      @click="close"
                    >
                      <p
                        class="line-clamp-2 text-sm font-medium"
                        :class="tab === 'unread' ? 'text-white' : 'text-slate-200'"
                      >
                        {{ n.title }}
                      </p>
                      <p class="mt-0.5 line-clamp-1 text-xs text-slate-500">
                        {{ formatWhen(n.createdAt) }}
                      </p>
                    </NuxtLink>
                  </li>
                </ul>
                <p v-else class="px-2 py-6 text-center text-sm text-slate-500">
                  {{
                    tab === 'unread'
                      ? 'Không có thông báo chưa đọc.'
                      : 'Chưa có thông báo đã đọc.'
                  }}
                </p>
              </template>
            </div>

            <div class="border-t border-white/10 px-3 py-2">
              <NuxtLink
                to="/notifications"
                class="block rounded-xl py-2 text-center text-sm font-medium text-teal-300 hover:text-teal-200"
                @click="close"
              >
                Xem tất cả
              </NuxtLink>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>
