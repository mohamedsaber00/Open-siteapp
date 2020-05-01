package com.msaber.openapiapp.ui.auth.state

import com.msaber.openapiapp.model.AuthToken

data class AuthViewState(
    var registrationFields: RegistrationFields? = RegistrationFields(),
    var loginFields: LoginFields? = LoginFields(),
    var authToken : AuthToken? = null)

data class RegistrationFields(
    var registration_email: String? = null,
    var registration_username: String? = null,
    var registration_password: String? = null,
    var registration_confirm_password: String? = null) {
    class RegistrationError {
        companion object {
            fun mustFillAllFields(): String {
                return "All fields are required"
            }

            fun passwordDoNotMatch(): String {
                return "password must match"
            }

            fun none(): String {
                return "None"
            }
        }
    }

    fun isValidForRegistration(): String {
        if (registration_email.isNullOrEmpty()
            or registration_username.isNullOrEmpty()
            or registration_password.isNullOrEmpty()
            or registration_confirm_password.isNullOrEmpty()
        ) {
            return RegistrationError.mustFillAllFields()
        }
        if (!registration_password.equals(registration_confirm_password)) {
            return RegistrationError.passwordDoNotMatch()
        }
        return RegistrationError.none()
    }
}

data class LoginFields(
    var login_email: String? = null,
    var login_password: String? = null
) {
    class LoginError {
        companion object {
            fun mustFillAllFields(): String {
                return "You can't login without an email and password."
            }

            fun none(): String {
                return "None"
            }
        }
    }

    fun isValidForLogin(): String {
        if (login_email.isNullOrEmpty()
            || login_password.isNullOrEmpty()
        ) {

            return LoginError.mustFillAllFields()
        }
        return LoginError.none()
    }

    override fun toString(): String {
        return "LoginState(email=$login_email, password=$login_password)"
    }
}


