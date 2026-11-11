import { ref } from 'vue';

export interface Notice {
  type: 'ok' | 'error' | 'warn';
  text: string;
}

export function useNotice() {
  const notice = ref<Notice | null>(null);
  const busy = ref(false);

  async function run<T>(work: () => Promise<T>, success?: string | ((result: T) => string)): Promise<T | undefined> {
    notice.value = null;
    busy.value = true;
    try {
      const result = await work();
      if (success) {
        notice.value = { type: 'ok', text: typeof success === 'function' ? success(result) : success };
      }
      return result;
    } catch (error) {
      notice.value = { type: 'error', text: error instanceof Error ? error.message : String(error) };
      return undefined;
    } finally {
      busy.value = false;
    }
  }

  return { notice, busy, run };
}
