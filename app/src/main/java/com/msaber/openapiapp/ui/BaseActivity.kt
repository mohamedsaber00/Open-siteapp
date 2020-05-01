package com.msaber.openapiapp.ui

import android.util.Log
import com.msaber.openapiapp.session.SessionManager
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity(), DataStateChangeListener {

    val TAG = "BaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onDataStateChange(dataState: DataState<*>?) {
        dataState?.let {
            GlobalScope.launch(Main) {
                displayProgressBar(it.loading.isLoading)

                it.error?.let { errorEvent ->
                    handleStateError(errorEvent)
                }
                it.data?.let {
                    it.response?.let { response ->
                        handleStateResponse(response)
                    }
                }
            }
        }
    }

    private fun handleStateResponse(event: Event<Response>) {
        event.getContentIfNotHandled()?.let {
            when (it.responseType) {
                is ResponseType.Toast -> {

                    it.message?.let {message ->
                        displayToast(message)
                    }
                }
                is ResponseType.Dialog -> {
                    it.message?.let {message ->
                        displaySuccessDialog(message)
                    }
                }
                is ResponseType.None -> {
                    Log.d(TAG, "handleStateError: ${it.message}")
                }
            }
        }
    }

    private fun handleStateError(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let {
            when (it.response.responseType) {
                is ResponseType.Toast -> {

                     it.response.message?.let {message ->
                          displayToast(message)
                     }
                }
                is ResponseType.Dialog -> {
                    it.response.message?.let {message ->
                        displayErrorDialog(message)
                    }
                }
                is ResponseType.None -> {
                    Log.e(TAG, "handleStateError: ${it.response.message}")
                }
            }
        }
    }

    abstract fun displayProgressBar(bool: Boolean)
}