package com.nch.todoapp

object Routes {
    const val LOGIN = "login"
    const val LIST = "list"
    const val CREATE = "create"
    const val DETAILS = "details/{todoId}"

    fun details(todoId: String) = "details/$todoId"
}
