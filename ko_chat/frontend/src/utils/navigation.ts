import type { Router } from 'vue-router'

export const navigateToError = async (router: Router, message?: string) => {
  await router.push({
    name: 'error',
    query: message ? { message } : undefined,
  })
}
