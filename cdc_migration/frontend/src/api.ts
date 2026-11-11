import { ref } from 'vue';

const WORKER_KEY = 'cdc-migration.worker';

function readWorker(): string {
  try {
    return localStorage.getItem(WORKER_KEY) ?? '';
  } catch {
    return '';
  }
}

export const worker = ref(readWorker());

export function saveWorker(value: string): void {
  worker.value = value;
  try {
    localStorage.setItem(WORKER_KEY, value.trim());
  } catch {
    return;
  }
}

export class ApiError extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
  }
}

async function request<T>(method: string, url: string, body?: BodyInit | object, extraHeaders: Record<string, string> = {}): Promise<T> {
  const headers: Record<string, string> = { ...extraHeaders };
  if (worker.value.trim()) {
    headers['X-Worker'] = encodeURIComponent(worker.value.trim());
  }
  let payload: BodyInit | undefined;
  if (body instanceof FormData) {
    payload = body;
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }
  let response: Response;
  try {
    response = await fetch(url, { method, headers, body: payload });
  } catch {
    throw new ApiError('서버에 연결할 수 없습니다. 백엔드(8085)가 실행 중인지 확인하세요.', 0);
  }
  const text = await response.text();
  let data: unknown = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }
  if (!response.ok) {
    const error = data as { message?: string; detail?: string } | null;
    const message = error?.message ? [error.message, error.detail].filter(Boolean).join(' - ') : `요청을 처리하지 못했습니다. (HTTP ${response.status})`;
    throw new ApiError(message, response.status);
  }
  return data as T;
}

export const api = {
  get: <T>(url: string) => request<T>('GET', url),
  post: <T>(url: string, body?: BodyInit | object, headers?: Record<string, string>) => request<T>('POST', url, body, headers),
  put: <T>(url: string, body?: object) => request<T>('PUT', url, body),
};

export function query(params: Record<string, string | number | undefined | null>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.append(key, String(value));
    }
  });
  const text = search.toString();
  return text ? `?${text}` : '';
}

export function formatDate(value?: string | null): string {
  return value ? new Date(value).toLocaleString('ko-KR', { hour12: false }) : '-';
}

export function formatBytes(value?: number | null): string {
  if (value === undefined || value === null) return '-';
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / 1024 / 1024).toFixed(1)} MB`;
}
