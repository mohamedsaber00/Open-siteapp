package com.msaber.openapiapp.ui.auth.state

sealed class AuthStateEvent {
    data class LoginAttemptEvent(val email: String,val password : String) : AuthStateEvent()
    data class RegisterAttemptEvent(val email: String,
    val userName: String,
    val password : String,
    val confirm_password : String): AuthStateEvent()

    object CheckPreviousAuthEvent : AuthStateEvent()
}

