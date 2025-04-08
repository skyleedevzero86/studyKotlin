package com.functionstudy.ones.ch04.template

class EmailTemplate(private val templateText: String) {
    fun invoke(person: Person): String {
        return templateText.replace("{{name}}", person.name)
    }
}
