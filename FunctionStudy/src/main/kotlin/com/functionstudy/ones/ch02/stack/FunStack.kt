package com.functionstudy.ones.ch02.stack

class FunStack<T> {
    private val elements = mutableListOf<T>()

    fun push(element: T) {
        elements.add(element)
    }

    fun pop(): T? {
        return if (elements.isNotEmpty()) {
            elements.removeAt(elements.size - 1)
        } else {
            null
        }
    }
    fun size(): Int {
        return elements.size
    }

    fun peek(): T? {
        return if (elements.isNotEmpty()) {
            elements[elements.size - 1]
        } else {
            null
        }
    }
}