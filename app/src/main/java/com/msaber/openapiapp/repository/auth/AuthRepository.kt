package com.msaber.openapiapp.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.util.PreferenceKeys
import com.codingwithmitch.openapi.util.SuccessHandling
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import com.msaber.openapiapp.api.auth.OpenApiAuthService
import com.msaber.openapiapp.di.auth.network_responses.LoginResponse
import com.msaber.openapiapp.di.auth.network_responses.RegistrationResponse
import com.msaber.openapiapp.model.AccountProperties
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.persistence.AccountPropertiesDao
import com.msaber.openapiapp.persistence.AuthTokenDao
import com.msaber.openapiapp.repository.JobManager
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
import com.msaber.openapiapp.util.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.msaber.openapiapp.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.msaber.openapiapp.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.msaber.openapiapp.util.GenericApiResponse
import kotlinx.coroutines.Job
import javax.inject.Inject


class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
): JobManager("AuthRepository")
{

    private val TAG: String = "AppDebug"

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>>{

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if(!loginFieldErrors.equals(LoginFields.LoginError.none())){
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<LoginResponse, Any, AuthViewState>(
            sessionManager.isInternetAvailable(),
            true,
            true,
            false
        ){

            // Ignore
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // Ignore
            override suspend fun updateLocalDB(cacheObject: Any?) {

            }

            // not used in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: ${response}")

                // Incorrect login credentials counts as a 200 response from server, so need to handle that
                if(response.body.response.equals(GENERIC_AUTH_ERROR)){
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                // Don't care about result here. Just insert if it doesn't exist b/c of foreign key relationship
                // with AuthToken
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if(result < 0){
                    return onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog()))
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                addJob("attemptLogin", job)
            }

        }.asLiveData()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>>{

        val registrationFieldErrors = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if(!registrationFieldErrors.equals(RegistrationFields.RegistrationError.none())){
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
            sessionManager.isInternetAvailable(),
            true,
            true,
            false
        ){
            // Ignore
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // Ignore
            override suspend fun updateLocalDB(cacheObject: Any?) {

            }

            // not used in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {

                Log.d(TAG, "handleApiSuccessResponse: ${response}")

                if(response.body.response.equals(GENERIC_AUTH_ERROR)){
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        response.body.username
                    )
                )

                // will return -1 if failure
                if(result1 < 0){
                    onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_ACCOUNT_PROPERTIES, ResponseType.Dialog()))
                    )
                    return
                }

                // will return -1 if failure
                val result2 = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if(result2 < 0){
                    onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                    ))
                    return
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                addJob("attemptRegistration", job)
            }

        }.asLiveData()
    }


    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>>{

        val previousAuthUserEmail: String? = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if(previousAuthUserEmail.isNullOrBlank()){
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            return returnNoTokenFound()
        }
        else{
            return object: NetworkBoundResource<Void, Any, AuthViewState>(
                sessionManager.isInternetAvailable(),
                false,
                false,
                false
            ){

                // Ignore
                override fun loadFromCache(): LiveData<AuthViewState> {
                    return AbsentLiveData.create()
                }

                // Ignore
                override suspend fun updateLocalDB(cacheObject: Any?) {

                }

                override suspend fun createCacheRequestAndReturn() {
                    accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                        Log.d(TAG, "createCacheRequestAndReturn: searching for token... account properties: ${accountProperties}")

                        accountProperties?.let {
                            if(accountProperties.pk > -1){
                                authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                    if(authToken != null){
                                        if(authToken.token != null){
                                            onCompleteJob(
                                                DataState.data(
                                                    AuthViewState(authToken = authToken)
                                                )
                                            )
                                            return
                                        }
                                    }
                                }
                            }
                        }
                        Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                        onCompleteJob(
                            DataState.data(
                                null,
                                Response(
                                    RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                    ResponseType.None()
                                )
                            )
                        )
                    }
                }

                // not used in this case
                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                }

                // not used in this case
                override fun createCall(): LiveData<GenericApiResponse<Void>> {
                    return AbsentLiveData.create()
                }

                override fun setJob(job: Job) {
                    addJob("checkPreviousAuthUser", job)
                }


            }.asLiveData()
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String){
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>>{
        return object: LiveData<DataState<AuthViewState>>(){
            override fun onActive() {
                super.onActive()
                value = DataState.data(null, Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None()))
            }
        }
    }

    private fun returnErrorResponse(errorMessage: String, responseType: ResponseType): LiveData<DataState<AuthViewState>>{
        Log.d(TAG, "returnErrorResponse: ${errorMessage}")

        return object: LiveData<DataState<AuthViewState>>(){
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }

}
