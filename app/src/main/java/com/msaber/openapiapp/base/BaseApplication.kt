package com.msaber.openapiapp.base

import android.app.Activity
import android.app.Application
import com.msaber.openapiapp.di.AppInjector
import com.msaber.openapiapp.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Inject

class BaseApplication: Application(),HasAndroidInjector{

    @Inject lateinit var androidInjector : DispatchingAndroidInjector<Any>

    override fun androidInjector() = androidInjector

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
    }
}