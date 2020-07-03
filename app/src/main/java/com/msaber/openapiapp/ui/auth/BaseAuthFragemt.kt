package com.msaber.openapiapp.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.msaber.openapiapp.di.Injectable
import com.msaber.openapiapp.viewmodels.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import javax.inject.Inject

abstract class BaseAuthFragment : Fragment(),Injectable{

    public val TAG = "BaseAuthFragment"

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this,providerFactory).get(AuthViewModel::class.java)
        }?: throw Exception("invalid Activity")
        //when ever a new fragment is in the view cancel active jobs
        cancelActiveJobs()
    }

     fun cancelActiveJobs(){
        viewModel.cancelActiveJobs()
    }
}