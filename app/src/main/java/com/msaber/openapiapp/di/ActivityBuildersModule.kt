package com.msaber.openapiapp.di

import com.msaber.openapiapp.di.auth.AuthFragmentBuildersModule
import com.msaber.openapiapp.di.auth.AuthModule
import com.msaber.openapiapp.di.auth.AuthScope
import com.msaber.openapiapp.di.auth.AuthViewModelModule
import com.msaber.openapiapp.ui.auth.AuthActivity
import com.msaber.openapiapp.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @ContributesAndroidInjector
    abstract fun contributeMainActivity() : MainActivity
}