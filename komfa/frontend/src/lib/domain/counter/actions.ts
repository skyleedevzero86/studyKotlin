/**
 * Domain: Counter 액션 (순수 함수)
 * 부수 효과 없음. 같은 입력 → 같은 출력.
 */
import type { Counter } from './types.js';

export const increment = (counter: Counter): Counter => ({
	value: counter.value + 1
});

export const decrement = (counter: Counter): Counter => ({
	value: counter.value - 1
});

export const add = (counter: Counter, delta: number): Counter => ({
	value: counter.value + delta
});
