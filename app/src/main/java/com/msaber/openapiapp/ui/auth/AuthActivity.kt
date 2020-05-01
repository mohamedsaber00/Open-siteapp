package com.msaber.openapiapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.msaber.openapiapp.R
import com.msaber.openapiapp.ui.BaseActivity
import com.msaber.openapiapp.ui.auth.state.AuthStateEvent
import com.msaber.openapiapp.ui.hide
import com.msaber.openapiapp.ui.main.MainActivity
import com.msaber.openapiapp.ui.visible
import com.msaber.openapiapp.viewmodels.ViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_auth.*
import javax.inject.Inject


class AuthActivity : BaseActivity(), NavController.OnDestinationChangedListener {


    lateinit var viewModel: AuthViewModel

    @Inject
    lateinit var provider: ViewModelProviderFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        viewModel = ViewModelProvider(this, provider).get(AuthViewModel::class.java)

        subscribeObservers()
        checkPreviousAuthUser()
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(this, Observer { dataState ->
            onDataStateChange(dataState)

            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let {
                            Log.d(TAG, "subscribeObservers: DataState $it")
                            viewModel.setAuthToken(it)
                        }
                    }
                }

            }
        })

        viewModel.viewState.observe(this, Observer {
            it.authToken?.let {
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, Observer { authtoken ->
            Log.d(TAG, "subscribeObservers: AuthToken : $authtoken")
            if (authtoken != null && authtoken.account_pk != -1 && authtoken.token != null) {
                navMainActivity()
            }
        })
    }

    fun checkPreviousAuthUser(){
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent)
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            progress_bar.visible()
        } else {
            progress_bar.hide()
        }
    }
}





