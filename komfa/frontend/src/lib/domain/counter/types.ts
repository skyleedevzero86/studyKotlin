/**
 * Domain: Counter
 * 순수한 도메인 타입. 프레임워크/UI에 의존하지 않음.
 */

export type Counter = Readonly<{
	value: number;
}>;

export const createCounter = (value: number): Counter => ({ value });
