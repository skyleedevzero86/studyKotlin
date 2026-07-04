import { ref, watch, type Ref } from 'vue'
import { fetchMySurveys } from '../api/surveyApi'
import router from '../router'
import { useAuth } from './useAuth'

export interface SurveyNotification {
  surveyId: number
  title: string
  description: string | null
}

const DISMISSED_SURVEY_POPUPS_KEY = 'kochat:dismissedSurveyPopups'

export const surveyNotification = ref<SurveyNotification | null>(null)
let notificationWs: WebSocket | null = null
let notificationWsReconnectTimer: ReturnType<typeof setTimeout> | undefined
let notificationWsIntentionalClose = false
let isListening = false
let hooksRegistered = false
let pendingCheckTimer: ReturnType<typeof setTimeout> | undefined

const readDismissedSurveyIds = (): Set<number> => {
  try {
    const raw = sessionStorage.getItem(DISMISSED_SURVEY_POPUPS_KEY)
    if (!raw) return new Set()
    return new Set(JSON.parse(raw) as number[])
  } catch {
    return new Set()
  }
}

const persistDismissedSurveyIds = (ids: Set<number>) => {
  sessionStorage.setItem(DISMISSED_SURVEY_POPUPS_KEY, JSON.stringify([...ids]))
}

const markSurveyPopupDismissed = (surveyId: number) => {
  const ids = readDismissedSurveyIds()
  ids.add(surveyId)
  persistDismissedSurveyIds(ids)
}

const clearSurveyPopupDismissed = (surveyId: number) => {
  const ids = readDismissedSurveyIds()
  if (!ids.delete(surveyId)) return
  persistDismissedSurveyIds(ids)
}

const showSurveyNotification = (payload: SurveyNotification) => {
  surveyNotification.value = payload
}

export const checkPendingSurveyNotifications = async (
  token: string,
  options?: { force?: boolean },
) => {
  if (!options?.force && surveyNotification.value) return
  try {
    const surveys = await fetchMySurveys(token)
    const pending = surveys.filter(
      (survey) => !survey.hasResponded && survey.canRespond && !survey.waitingForStart,
    )
    const dismissed = readDismissedSurveyIds()
    const target = pending.find((survey) => !dismissed.has(survey.surveyId))
    if (!target) return
    showSurveyNotification({
      surveyId: target.surveyId,
      title: target.title,
      description: target.description,
    })
  } catch {
  }
}

const schedulePendingSurveyCheck = (token: string, delayMs = 300) => {
  clearTimeout(pendingCheckTimer)
  pendingCheckTimer = setTimeout(() => {
    void checkPendingSurveyNotifications(token)
  }, delayMs)
}

const runPendingSurveyCheck = (token: string) => {
  void checkPendingSurveyNotifications(token)
  schedulePendingSurveyCheck(token)
}

const connectNotificationWs = (token: string) => {
  if (notificationWs && notificationWs.readyState === WebSocket.OPEN) return
  notificationWsIntentionalClose = false
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/api/v1/ws/chat?token=${encodeURIComponent(token)}`
  notificationWs = new WebSocket(wsUrl)
  notificationWs.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'SURVEY_NOTIFICATION') {
        clearSurveyPopupDismissed(data.surveyId)
        showSurveyNotification({
          surveyId: data.surveyId,
          title: data.title,
          description: data.description ?? null,
        })
      }
    } catch {
    }
  }
  notificationWs.onopen = () => {
    runPendingSurveyCheck(token)
  }
  notificationWs.onerror = () => {
    runPendingSurveyCheck(token)
  }
  notificationWs.onclose = () => {
    notificationWs = null
    if (!notificationWsIntentionalClose && isListening) {
      notificationWsReconnectTimer = setTimeout(() => {
        const stored = localStorage.getItem('accessToken')?.trim()
        if (stored && isListening) {
          connectNotificationWs(stored)
        }
      }, 3000)
    }
  }
}

export const disconnectSurveyNotificationWs = () => {
  notificationWsIntentionalClose = true
  isListening = false
  clearTimeout(notificationWsReconnectTimer)
  clearTimeout(pendingCheckTimer)
  if (notificationWs) {
    notificationWs.close(1000)
    notificationWs = null
  }
}

export const useSurveyNotification = (): {
  surveyNotification: Ref<SurveyNotification | null>
  dismissSurveyNotification: () => void
  refreshPendingSurveyNotifications: () => Promise<void>
} => {
  const { accessToken, isAuthenticated } = useAuth()

  if (!hooksRegistered) {
    hooksRegistered = true

    watch(
      isAuthenticated,
      (authenticated) => {
        if (authenticated && accessToken.value) {
          isListening = true
          connectNotificationWs(accessToken.value)
          runPendingSurveyCheck(accessToken.value)
          return
        }
        disconnectSurveyNotificationWs()
        surveyNotification.value = null
      },
      { immediate: true },
    )

    watch(accessToken, (token) => {
      if (!isListening || !token) return
      disconnectSurveyNotificationWs()
      isListening = true
      connectNotificationWs(token)
      runPendingSurveyCheck(token)
    })

    router.afterEach((to) => {
      if (!to.meta.requiresAuth) return
      const token = accessToken.value ?? localStorage.getItem('accessToken')?.trim()
      if (!token) return
      schedulePendingSurveyCheck(token)
    })
  }

  const dismissSurveyNotification = () => {
    if (surveyNotification.value) {
      markSurveyPopupDismissed(surveyNotification.value.surveyId)
    }
    surveyNotification.value = null
  }

  const refreshPendingSurveyNotifications = async () => {
    const token = accessToken.value ?? localStorage.getItem('accessToken')?.trim()
    if (!token) return
    await checkPendingSurveyNotifications(token)
  }

  return {
    surveyNotification,
    dismissSurveyNotification,
    refreshPendingSurveyNotifications,
  }
}
