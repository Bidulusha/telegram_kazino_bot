package ru.template.telegram.bot.enums

enum class CommandCode(val command: String, val desc: String) {
    START("start", "start work"),
    USER_INFO("user_info", "user info"),
    BUTTON("button", "button yes no")
}