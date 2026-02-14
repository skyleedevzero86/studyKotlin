/**
 * Application: Counter 유스케이스
 * 도메인 규칙을 조합. 필요 시 인프라(저장소)에 위임.
 */
import { createCounter, increment, decrement, add } from '$lib/domain/counter/index.js';
import type { Counter } from '$lib/domain/counter/index.js';

export type CounterStore = {
	get: () => Counter;
	set: (c: Counter) => void;
};

const createUseCounter = (store: CounterStore) => {
	const getCounter = (): Counter => store.get();
	const setCounter = (c: Counter): void => store.set(c);

	return {
		getCounter,
		init: () => setCounter(createCounter(0)),
		inc: () => setCounter(increment(getCounter())),
		dec: () => setCounter(decrement(getCounter())),
		addBy: (delta: number) => setCounter(add(getCounter(), delta))
	};
};

export { createUseCounter };
