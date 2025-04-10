package com.karchitecture.global.util

inline fun throwWhen(
    condition: Boolean,
    supplier: () -> RuntimeException,
) {
    if (condition) throw supplier()
}
