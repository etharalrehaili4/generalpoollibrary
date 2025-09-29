package com.example.auth.utils

class Validator {
    val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")

    fun isPasswordValid(password: String): Boolean = passwordRegex.matches(password)

    fun isUsernameValid(agentId: String): Boolean =
        // agentId.length == AGENT_ID_LENGTH &&
        agentId.all {
            it.isDigit()
        }
}
