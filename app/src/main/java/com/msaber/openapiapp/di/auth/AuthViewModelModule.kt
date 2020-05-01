package com.msaber.openapiapp.di.auth

import androidx.lifecycle.ViewModel
import com.msaber.openapiapp.di.ViewModelKey
import com.msaber.openapiapp.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}