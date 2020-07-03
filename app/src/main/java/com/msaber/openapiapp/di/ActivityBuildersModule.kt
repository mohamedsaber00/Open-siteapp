package com.msaber.openapiapp.di

import com.msaber.openapiapp.di.auth.AuthFragmentBuildersModule
import com.msaber.openapiapp.di.auth.AuthModule
import com.msaber.openapiapp.di.auth.AuthScope
import com.msaber.openapiapp.di.auth.AuthViewModelModule
import com.msaber.openapiapp.di.main.MainFragmentBuildersModule
import com.msaber.openapiapp.di.main.MainModule
import com.msaber.openapiapp.di.main.MainScope
import com.msaber.openapiapp.di.main.MainViewModelModule
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

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity
}