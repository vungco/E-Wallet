<script setup lang="ts">
import type { NotificationItem } from '~/types/api'

type NotifTab = 'unread' | 'read'

definePageMeta({ middleware: 'auth' })

const { refreshUnreadCount, fetchPage } = useNotifications()

const activeTab = ref<NotifTab>('unread')

const unreadItems = ref<NotificationItem[]>([])
const readItems = ref<NotificationItem[]>([])
const unreadCursor = ref<string | undefined>(undefined)
const readCursor = ref<string | undefined>(undefined)
const unreadMore = ref(true)
const readMore = ref(true)
const loadingUnread = ref(false)
const loadingRead = ref(false)
const readTabLoaded = ref(false)
const err = ref('')

async function loadUnread(append: boolean) {
  if (loadingUnread.value) return
  if (append && !unreadMore.value) return
  loadingUnread.value = true
  err.value = ''
  try {
    const c = append ? unreadCursor.value : undefined
    const page = await fetchPage('unread', c, 15)
    if (append) unreadItems.value = [...unreadItems.value, ...page.items]
    else unreadItems.value = page.items
    unreadCursor.value = page.nextCursor || undefined
    unreadMore.value = page.hasMore
    await refreshUnreadCount()
  } catch (e) {
    err.value = e instanceof Error ? e.message : 'Không tải được danh sách.'
  } finally {
    loadingUnread.value = false
  }
}

async function loadRead(append: boolean) {
  if (loadingRead.value) return
  if (append && !readMore.value) return
  loadingRead.value = true
  err.value = ''
  try {
    const c = append ? readCursor.value : undefined
    const page = await fetchPage('read', c, 15)
    if (append) readItems.value = [...readItems.value, ...page.items]
    else readItems.value = page.items
    readCursor.value = page.nextCursor || undefined
    readMore.value = page.hasMore
  } catch (e) {
    err.value = e instanceof Error ? e.message : 'Không tải được danh sách.'
  } finally {
    loadingRead.value = false
  }
}

function setTab(t: NotifTab) {
  activeTab.value = t
  if (t === 'read' && !readTabLoaded.value) {
    readTabLoaded.value = true
    void loadRead(false)
  }
}

const currentItems = computed(() =>
  activeTab.value === 'unread' ? unreadItems.value : readItems.value,
)
const currentLoading = computed(() =>
  activeTab.value === 'unread' ? loadingUnread.value : loadingRead.value,
)
const currentHasMore = computed(() =>
  activeTab.value === 'unread' ? unreadMore.value : readMore.value,
)

function loadMoreCurrent() {
  if (activeTab.value === 'unread') void loadUnread(true)
  else void loadRead(true)
}

function formatWhen(iso: string | null) {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleString('vi-VN')
  } catch {
    return iso
  }
}

onMounted(() => {
  void loadUnread(false)
})
</script>

<template>
  <div class="mx-auto max-w-3xl px-4 py-10">
    <h1 class="text-2xl font-semibold text-white">Thông báo</h1>
    <p class="mt-1 text-sm text-slate-500">Chọn tab để xem danh sách tương ứng.</p>

    <p v-if="err" class="mt-4 rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
      {{ err }}
    </p>

    <div class="mt-8">
      <div
        class="flex rounded-xl border border-white/10 bg-white/[0.04] p-1"
        role="tablist"
        aria-label="Thông báo theo trạng thái"
      >
        <button
          type="button"
          role="tab"
          :aria-selected="activeTab === 'unread'"
          class="flex-1 rounded-lg px-4 py-3 text-sm font-semibold transition"
          :class="
            activeTab === 'unread'
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
          :aria-selected="activeTab === 'read'"
          class="flex-1 rounded-lg px-4 py-3 text-sm font-semibold transition"
          :class="
            activeTab === 'read'
              ? 'bg-white/10 text-white shadow-sm'
              : 'text-slate-400 hover:text-slate-200'
          "
          @click="setTab('read')"
        >
          Đã đọc
        </button>
      </div>

      <div role="tabpanel" class="mt-6">
        <div v-if="currentLoading && !currentItems.length" class="py-12 text-center text-sm text-slate-500">
          Đang tải…
        </div>
        <template v-else>
          <ul class="space-y-2">
            <li v-for="n in currentItems" :key="n.id">
              <NuxtLink
                :to="`/notifications/${n.id}`"
                class="block rounded-2xl border px-4 py-3 transition"
                :class="
                  activeTab === 'unread'
                    ? 'border-white/10 bg-white/5 hover:border-teal-500/40'
                    : 'border-white/5 bg-white/[0.02] hover:border-white/15'
                "
              >
                <p
                  class="font-medium"
                  :class="activeTab === 'unread' ? 'text-white' : 'text-slate-200'"
                >
                  {{ n.title }}
                </p>
                <p
                  class="mt-1 line-clamp-2 text-sm"
                  :class="activeTab === 'unread' ? 'text-slate-400' : 'text-slate-500'"
                >
                  {{ n.body }}
                </p>
                <p
                  class="mt-2 text-xs"
                  :class="activeTab === 'unread' ? 'text-slate-500' : 'text-slate-600'"
                >
                  {{ formatWhen(n.createdAt) }}
                </p>
              </NuxtLink>
            </li>
          </ul>
          <p
            v-if="!currentItems.length && !currentLoading"
            class="mt-4 text-center text-sm text-slate-500"
          >
            {{ activeTab === 'unread' ? 'Không có thông báo chưa đọc.' : 'Chưa có thông báo đã đọc.' }}
          </p>
          <div v-if="currentHasMore && currentItems.length" class="mt-6 flex justify-center">
            <button
              type="button"
              class="ew-btn-ghost text-sm"
              :disabled="currentLoading"
              @click="loadMoreCurrent"
            >
              {{ currentLoading ? 'Đang tải…' : 'Tải thêm' }}
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
