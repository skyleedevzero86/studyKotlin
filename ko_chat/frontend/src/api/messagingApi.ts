import { getJson, postJson } from './http'
import type { MessagingOperationsSnapshot } from '../types/messaging'

const basePath = '/api/v1/admin/messaging'

export const getMessagingOperations = (
  token: string,
): Promise<MessagingOperationsSnapshot> =>
  getJson(`${basePath}/operations`, token)

export const requeueFailedOutbox = (
  token: string,
  limit = 50,
): Promise<{ requeued: number }> =>
  postJson(`${basePath}/outbox/requeue-failed?limit=${limit}`, {}, token)

export const replayDlqEvent = (
  token: string,
  dlqEventId: number,
): Promise<{ replayed: boolean }> =>
  postJson(`${basePath}/dlq/${dlqEventId}/replay`, {}, token)
