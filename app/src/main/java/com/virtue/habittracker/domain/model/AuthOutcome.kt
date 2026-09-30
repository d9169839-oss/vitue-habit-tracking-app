package com.virtue.habittracker.domain.model
sealed interface AuthOutcome {
    data class Success(val user: User) : AuthOutcome
    data class Failure(val message: String) : AuthOutcome
}
