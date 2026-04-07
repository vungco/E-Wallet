export default defineNuxtPlugin(() => {
  const { hydrateFromStorage } = useAuth()
  hydrateFromStorage()
})
