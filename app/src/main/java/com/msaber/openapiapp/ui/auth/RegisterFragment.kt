package com.msaber.openapiapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.msaber.openapiapp.R
import com.msaber.openapiapp.ui.auth.state.AuthStateEvent
import com.msaber.openapiapp.ui.auth.state.RegistrationFields
import kotlinx.android.synthetic.main.fragment_register.*


// TODO: Rename parameter arguments, choose names that match

class RegisterFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Register onViewCreated: ${viewModel.hashCode()}")
        subscribeObservers()

        register_button.setOnClickListener {
            register()
        }

    }
    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.registrationFields?.let { registrationFields ->
                registrationFields.registration_email?.let { email -> input_email.setText(email) }
                registrationFields.registration_password?.let { password ->
                    input_password?.setText(
                        password
                    )
                }
                registrationFields.registration_confirm_password?.let { confirm_password ->
                    input_password_confirm.setText(
                        confirm_password
                    )
                }
                registrationFields.registration_username?.let { username ->
                    input_username.setText(
                        username
                    )
                }
            }
        })
    }
    fun register(){
        viewModel.setStateEvent(AuthStateEvent.RegisterAttemptEvent(
            input_email.text.toString(),
            input_username.text.toString(),
            input_password.text.toString(),
            input_password_confirm.text.toString())
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationField(
            RegistrationFields(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )
    }


}