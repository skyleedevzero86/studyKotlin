import type { AppError } from "./errors";

export type Result<T, E = AppError> =
    | { success: true; value: T }
    | { success: false; error: E };

export function ok<T, E = AppError>(value: T): Result<T, E> {
    return { success: true, value };
}

export function err<T, E = AppError>(error: E): Result<T, E> {
    return { success: false, error };
}

export function map<T, U, E>(r: Result<T, E>, f: (t: T) => U): Result<U, E> {
    return r.success ? ok(f(r.value)) : r;
}

export function flatMap<T, U, E>(
    r: Result<T, E>,
    f: (t: T) => Result<U, E>
): Result<U, E> {
    return r.success ? f(r.value) : r;
}

export function fold<T, E, R>(
    r: Result<T, E>,
    onSuccess: (t: T) => R,
    onFailure: (e: E) => R
): R {
    return r.success ? onSuccess(r.value) : onFailure(r.error);
}

export function getOrElse<T, E>(r: Result<T, E>, defaultValue: T): T {
    return r.success ? r.value : defaultValue;
}
