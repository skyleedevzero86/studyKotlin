package com.koroutines.elvisAndScope.scopefunction

fun main() {
    val name: String? = "홍길동"
    name?.let {
        println("이름은 ${it.length} 글자")
    }

    val name2: String? = null
    val result2 = name2
        ?.let { it.uppercase() }
        ?: "default"
    println(result2) // default
}

fun main3() {
    val user: User = User(
        name = "jay",
        age = 26
    ).apply {
        this.name = "jetty"
        this.age = 27
    }

    val nameLength = user.run {
        this.name.length
    }

    val updatedUser = user.also {
        println("name: ${it.name}, age: ${it.age}, length: $nameLength")
    }
}

data class User(var name: String, var age: Int)
