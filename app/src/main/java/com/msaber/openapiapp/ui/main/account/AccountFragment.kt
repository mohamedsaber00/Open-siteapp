package com.msaber.openapiapp.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.msaber.openapiapp.R
import com.msaber.openapiapp.model.AccountProperties

import com.msaber.openapiapp.ui.main.account.state.AccountStateEvent
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : BaseAccountFragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        change_password.setOnClickListener{
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }
        edit_button.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
        }

        logout_button.setOnClickListener {
            viewModel.logout()
        }

        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            Log.d(TAG, "AccountFragment: DataState: $dataState")
            stateChangeListener.onDataStateChange(dataState)
            if(dataState != null){
                dataState.data?.let { data ->
                    data.data?.let{ event ->
                        event.getContentIfNotHandled()?.let{ viewState ->
                            viewState.accountProperties?.let{ accountProperties ->
                                Log.d(TAG, "AccountFragment, DataState: ${accountProperties}")
                                viewModel.setAccountPropertiesData(accountProperties)
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState->
            Log.d(TAG, "AccountFragment, ViewState: ${viewState}")
            if(viewState != null){
                viewState.accountProperties?.let{
                    setAccountDataFields(it)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(AccountStateEvent.GetAccountPropertiesEvent())
    }

    private fun setAccountDataFields(accountProperties: AccountProperties){
        email?.setText(accountProperties.email)
        username?.setText(accountProperties.username)
    }


}