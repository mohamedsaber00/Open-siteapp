package com.msaber.openapiapp.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.msaber.openapiapp.api.GenericResponse
import com.msaber.openapiapp.api.main.OpenApiMainService
import com.msaber.openapiapp.model.AccountProperties
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.persistence.AccountPropertiesDao
import com.msaber.openapiapp.repository.JobManager
import com.msaber.openapiapp.repository.NetworkBoundResource
import com.msaber.openapiapp.session.SessionManager
import com.msaber.openapiapp.ui.DataState
import com.msaber.openapiapp.ui.Response
import com.msaber.openapiapp.ui.ResponseType
import com.msaber.openapiapp.ui.main.account.state.AccountViewState
import com.msaber.openapiapp.util.AbsentLiveData
import com.msaber.openapiapp.util.ApiSuccessResponse
import com.msaber.openapiapp.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
):JobManager("AccountRepository") {

    private val TAG = "AccountRepository"


    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isInternetAvailable(),
                true,
                false,
                true
            ) {


            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {

                //The parameter is same argument we needs on the website api
                return openApiMainService.getAccountProperties("Token ${authToken.token}")
            }

            override fun setJob(job: Job) {
               addJob("getAccountProperties",job)
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                //Return LiveData<AccountProperties> then use Switch map to convert it to liveData<AccountViewState> to bind it to the view
                return accountPropertiesDao.searchByPk(authToken.account_pk!!).switchMap {
                    object : LiveData<AccountViewState>() {
                        override fun onActive() {
                            super.onActive()
                            value = AccountViewState(it)
                        }
                    }
                }
            }

            override suspend fun updateLocalDB(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

            override suspend fun createCacheRequestAndReturn() {
                //updating liveData so should use Main thread
                withContext(Main) {
                    //finish by view the db cache by the latest data
                    result.addSource(loadFromCache()) { viewState ->
                        onCompleteJob(DataState.data(data = viewState, response = null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDB(response.body)
                createCacheRequestAndReturn()
            }

        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isInternetAvailable(),
            true,
            true,
            false
        ) {
            //not used in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDB(null)
                withContext(Main) {
                    //finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDB(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )

            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties",job)


            }


        }.asLiveData()
    }

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isInternetAvailable(),
            true,
            true,
            false
        ) {
            //Not applicable
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Main){
                    onCompleteJob(DataState.data(data = null,response = Response(response.body.response,ResponseType.Toast())))
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.updatePassword(
                    "Token ${authToken.token}",
                    currentPassword,
                    newPassword,
                    confirmNewPassword
                )
            }
            //Not applicable

            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }
            //Not applicable

            override suspend fun updateLocalDB(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
               addJob("updatePassword",job)
            }

        }.asLiveData()
    }


}