package com.functionstudy.ones.ch03.items.actions

data class DdtActions(val actionName: String, val description: String){
    fun execute() {

        println("Executing action: $actionName - $description")
    }
}