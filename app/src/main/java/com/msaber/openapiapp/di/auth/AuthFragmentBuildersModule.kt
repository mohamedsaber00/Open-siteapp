package com.msaber.openapiapp.di.auth

import com.msaber.openapiapp.ui.auth.ForgetPasswordFragment
import com.msaber.openapiapp.ui.auth.LauncherFragment
import com.msaber.openapiapp.ui.auth.LoginFragment
import com.msaber.openapiapp.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgetPasswordFragment

}