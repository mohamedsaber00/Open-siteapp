package com.msaber.openapiapp.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.msaber.openapiapp.persistence.AuthTokenDao
import com.codingwithmitch.openapi.util.PreferenceKeys
import com.codingwithmitch.openapi.util.SuccessHandling
import com.msaber.openapiapp.api.auth.OpenApiAuthService
import com.msaber.openapiapp.di.auth.network_responses.LoginResponse
import com.msaber.openapiapp.di.auth.network_responses.RegistrationResponse
import com.msaber.openapiapp.model.AccountProperties
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.persistence.AccountPropertiesDao
import com.msaber.openapiapp.repository.NetworkBoundResource
import com.msaber.openapiapp.session.SessionManager
import com.msaber.openapiapp.ui.DataState
import com.msaber.openapiapp.ui.Response
import com.msaber.openapiapp.ui.ResponseType
import com.msaber.openapiapp.ui.auth.state.AuthViewState
import com.msaber.openapiapp.ui.auth.state.LoginFields
import com.msaber.openapiapp.ui.auth.state.RegistrationFields
import com.msaber.openapiapp.util.AbsentLiveData
import com.msaber.openapiapp.util.ApiSuccessResponse
import com.msaber.openapiapp.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.msaber.openapiapp.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.msaber.openapiapp.util.GenericApiResponse
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository @Inject constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
) {
    private val TAG = "AuthRepository"

    private var repositoryJob: Job? = null

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldError = LoginFields(email, password).isValidForLogin()
        if (!loginFieldError.equals(LoginFields.LoginError.none())) {
            Log.d(TAG, "attemptLogin: Field is none")
            return returnErrorResponse(loginFieldError, ResponseType.Dialog())
        }
        return object :
            NetworkBoundResource<LoginResponse, AuthViewState>(
                sessionManager.isInternetAvailable(),
                true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: $response")
                //incorrect
                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                // Some Success will return Ok Even if the request denied like An already user exist .. password doen't match
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email, ""
                    )
                )

                //will return -1 if failure

                val result = authTokenDao.insert(AuthToken(response.body.pk, response.body.token))

                //if there is an error we don;t want the REAL ON COMPLETE IS DONE
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog()))
                    )
                }

                saveAuthenticationUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                response.body.pk,
                                response.body.token
                            )
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override suspend fun createCacheRequestAndReturn() {

            }

        }.asLiveData()
    }

    fun cancelActiveJob() {
        Log.d(TAG, "cancelActiveJob: cancelling any active job")
        repositoryJob?.cancel()
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType.Dialog
    ): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(Response(errorMessage, responseType))
            }
        }
    }

    fun attemptRegistration(
        email: String,
        userName: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {
        val registrationFieldsError =
            RegistrationFields(email, userName, password, confirmPassword).isValidForRegistration()
        if (!registrationFieldsError.equals(RegistrationFields.RegistrationError.none())) {
            return returnErrorResponse(registrationFieldsError, ResponseType.Dialog())
        }
        return object :
            NetworkBoundResource<RegistrationResponse, AuthViewState>(
                sessionManager.isInternetAvailable(),
                true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {

                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                // Some Succes will return Ok Even if the request denied like An already user exist .. password doesn't match
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email, ""
                    )
                )

                //will return -1 if failure

                val result = authTokenDao.insert(AuthToken(response.body.pk, response.body.token))

                //if there is an error we don;t want the REAL ON COMPLETE IS DONE
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog()))
                    )
                }

                saveAuthenticationUserToPrefs(email)


                //I don;t care about the result.. Just insert if it doesn't exist b/b foreign key relationship
                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                response.body.pk,
                                response.body.token
                            )
                        )
                    )
                )

            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, userName, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            //won't be used
            override suspend fun createCacheRequestAndReturn() {

            }
        }.asLiveData()

    }

    fun checkPreviousUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthEmail = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)
        if (previousAuthEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousUser: Not previous Authenticated user found")
            return returnNoTokenFound()
        }
        return object :
            NetworkBoundResource<Void, AuthViewState>(sessionManager.isInternetAvailable(), false) {
            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthEmail).let { accountProperties ->
                    Log.d(TAG, "createCacheRequestAndReturn: searching for token")

                    accountProperties?.let {
                        if (accountProperties.pk > -1) {
                            authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                if (authToken != null) {
                                    onCompleteJob(
                                        DataState.data(
                                            data = AuthViewState(authToken = authToken)
                                        )
                                    )
                                    return
                                }
                            }
                        }
                    }

                    Log.d(TAG, "createCacheRequestAndReturn: The auth user not found ")
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                ResponseType.None()
                            )
                        )
                    )
                }
            }

            //Not used.. not an request
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {

            }

            //Not used cause not internet request
            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }
        }.asLiveData()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(
                        SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                        ResponseType.None()
                    )
                )
            }
        }

    }

    private fun saveAuthenticationUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

}