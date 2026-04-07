<script setup lang="ts">
const { toasts, dismiss } = useTransferAlerts()
</script>

<template>
  <Teleport to="body">
    <div
      class="pointer-events-none fixed bottom-0 right-0 z-[100] flex max-h-screen w-full max-w-md flex-col gap-3 p-4 sm:p-6"
    >
      <TransitionGroup name="toast" tag="div" class="flex flex-col gap-3">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="pointer-events-auto overflow-hidden rounded-2xl border shadow-2xl backdrop-blur-xl"
          :class="
            t.variant === 'success'
              ? 'border-emerald-500/40 bg-emerald-950/90'
              : t.variant === 'error'
                ? 'border-red-500/40 bg-red-950/90'
                : 'border-slate-500/40 bg-slate-900/90'
          "
        >
          <div class="flex gap-3 p-4">
            <div
              class="mt-0.5 flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-lg font-bold"
              :class="
                t.variant === 'success'
                  ? 'bg-emerald-500/20 text-emerald-300'
                  : t.variant === 'error'
                    ? 'bg-red-500/20 text-red-300'
                    : 'bg-slate-500/20 text-slate-300'
              "
            >
              {{ t.variant === 'success' ? '✓' : t.variant === 'error' ? '!' : 'i' }}
            </div>
            <div class="min-w-0 flex-1">
              <p class="font-semibold text-white">{{ t.title }}</p>
              <p class="mt-1 whitespace-pre-wrap text-sm text-slate-300">{{ t.body }}</p>
            </div>
            <button
              type="button"
              class="shrink-0 rounded-lg px-2 py-1 text-slate-500 hover:bg-white/10 hover:text-white"
              aria-label="Đóng"
              @click="dismiss(t.id)"
            >
              ×
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-move,
.toast-enter-active,
.toast-leave-active {
  transition: all 0.35s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(1rem);
}
</style>
