/**
 * Infrastructure: 메모리 기반 Counter 저장소
 * 나중에 localStorage/API로 교체 가능.
 */
import type { Counter } from '$lib/domain/counter/index.js';
import type { CounterStore } from '$lib/application/counter/useCounter.js';

export const createMemoryCounterStore = (initial: Counter): CounterStore => {
	let state: Counter = initial;
	return {
		get: () => state,
		set: (c: Counter) => {
			state = c;
		}
	};
};
