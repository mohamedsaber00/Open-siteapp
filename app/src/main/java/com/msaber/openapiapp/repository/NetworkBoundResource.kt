package com.msaber.openapiapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.msaber.openapiapp.ui.DataState
import com.msaber.openapiapp.ui.Response
import com.msaber.openapiapp.ui.ResponseType
import com.msaber.openapiapp.util.*
import com.msaber.openapiapp.util.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.msaber.openapiapp.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.msaber.openapiapp.util.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.msaber.openapiapp.util.constant.Constants.Companion.NETWORK_TIMEOUT
import com.msaber.openapiapp.util.constant.Constants.Companion.TESTING_NETWORK_DELAY
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

@OptIn(InternalCoroutinesApi::class)
abstract class NetworkBoundResource<ResponseObject, ViewStateType>(
    isNetworkAvailable: Boolean,
    isNetworkRequest: Boolean
) {

    private val TAG = "NetworkBoundResource"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null))

        if (isNetworkRequest) {
            if (isNetworkAvailable) {
                // 2 coroutines start in the same time and only on will show resule : success ot rime out
                coroutineScope.launch {
                    delay(TESTING_NETWORK_DELAY)

                    withContext(Main) {
                        //Make network call
                        val apiResponse = createCall()
                        result.addSource(apiResponse) { response ->
                            result.removeSource(apiResponse)
                            coroutineScope.launch {
                                handleNetworkCall(response)
                            }
                        }
                    }
                }

                //cancel the job above after time out
                GlobalScope.launch(IO) {
                    delay(NETWORK_TIMEOUT)
                    if (!job.isCompleted) {
                        Log.e(TAG, "NetworkBoundResource: JOB NETWORK TIMEOUT.")
                        job.cancel(CancellationException(ErrorHandling.UNABLE_TO_RESOLVE_HOST))
                    }
                }
            } else {
                onErrorReturn(
                    UNABLE_TODO_OPERATION_WO_INTERNET,
                    shouldUseDialog = true,
                    shouldUseToats = false
                )
            }
        } else {
            //Here the requests which doesn't need the cache
            coroutineScope.launch {
                delay(TESTING_NETWORK_DELAY)

                //View data from Cache only
                createCacheRequestAndReturn( )
            }
        }

    }

    private suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {
        when (response) {
            is ApiSuccessResponse -> {
                handleApiSuccessResponse(response)
            }
            is ApiErrorResponse -> {
                Log.e(TAG, "handleNetworkCall: ${response.errorMessage}")
                onErrorReturn(
                    errorMessage = response.errorMessage,
                    shouldUseDialog = true,
                    shouldUseToats = false
                )

            }
            is ApiEmptyResponse -> {
                Log.e(TAG, "handleNetworkCall: Empty Resource")
                onErrorReturn(
                    errorMessage = "Empty response",
                    shouldUseDialog = true,
                    shouldUseToats = false
                )

            }

            null -> TODO()
        }
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToats: Boolean) {
        var msg = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()
        if (msg == null) {
            msg = ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(msg)) {
            msg = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }

        if (shouldUseToats) {
            responseType = ResponseType.Toast()
        }
        if (useDialog) {
            responseType = ResponseType.Dialog()
        }
        onCompleteJob(DataState.error(Response(message = msg, responseType = responseType)))
    }

    private fun initNewJob(): Job {
        Log.d(TAG, "initNewJob: called ")
        job = Job()
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object :
            CompletionHandler {
            override fun invoke(cause: Throwable?) {
                if (job.isCancelled) {
                    Log.d(TAG, "invoke: Job has been cancelled")
                    cause?.let {
                        //TODO show an error
                        onErrorReturn(it.message, shouldUseDialog = false, shouldUseToats = true)
                    } ?: onErrorReturn(
                        ERROR_UNKNOWN,
                        shouldUseDialog = false,
                        shouldUseToats = true
                    )
                } else if (job.isCompleted) {
                    Log.d(TAG, "invoke: Job is completed")
                    //Do nothing
                }
            }

        })
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>
    abstract suspend fun createCacheRequestAndReturn()

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)
    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>
    abstract fun setJob(job: Job)
}