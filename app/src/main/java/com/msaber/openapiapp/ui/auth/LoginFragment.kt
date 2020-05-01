package com.msaber.openapiapp.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.msaber.openapiapp.R
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.ui.auth.state.AuthStateEvent
import com.msaber.openapiapp.ui.auth.state.LoginFields
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : BaseAuthFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Login onViewCreated: ${viewModel.hashCode()}")
        subscribeObservers()
        login_button.setOnClickListener {
            login()
        }
    }
    private fun subscribeObservers() {
        Log.d(TAG, "subscribeObservers: ")
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.loginFields?.let { loginFields ->
                loginFields.login_email?.let {
                    Log.d(TAG, "subscribeObservers: $it")
                    input_email.setText(it)
                }
                loginFields.login_password?.let {
                    Log.d(TAG, "subscribeObservers: $it")
                    input_password.setText(it)
                }
            }
        })
    }

    fun login(){
        viewModel.setStateEvent(
            AuthStateEvent.LoginAttemptEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

}