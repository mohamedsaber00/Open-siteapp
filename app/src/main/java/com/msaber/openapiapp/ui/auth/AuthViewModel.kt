package com.msaber.openapiapp.ui.auth

import androidx.lifecycle.LiveData
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.repository.auth.AuthRepository
import com.msaber.openapiapp.ui.BaseViewModel
import com.msaber.openapiapp.ui.DataState
import com.msaber.openapiapp.ui.auth.state.AuthStateEvent
import com.msaber.openapiapp.ui.auth.state.AuthStateEvent.*
import com.msaber.openapiapp.ui.auth.state.AuthViewState
import com.msaber.openapiapp.ui.auth.state.LoginFields
import com.msaber.openapiapp.ui.auth.state.RegistrationFields
import com.msaber.openapiapp.util.AbsentLiveData
import javax.inject.Inject

class AuthViewModel @Inject constructor(val authRepository: AuthRepository) :
    BaseViewModel<AuthStateEvent, AuthViewState>() {

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    fun setRegistrationField(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        _viewState.value = update
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        return when (stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }
            is RegisterAttemptEvent -> {
                authRepository.attemptRegistration(
                    stateEvent.email,stateEvent.userName,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }
            is CheckPreviousAuthEvent -> {
                authRepository.checkPreviousUser()
            }
        }
    }

    fun cancelActiveJobs(){
        authRepository.cancelActiveJob()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}