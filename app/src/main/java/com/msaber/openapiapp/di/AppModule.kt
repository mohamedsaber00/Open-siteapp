package com.msaber.openapiapp.di


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.msaber.openapiapp.persistence.AuthTokenDao
import com.codingwithmitch.openapi.util.PreferenceKeys
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import com.msaber.openapiapp.R
import com.msaber.openapiapp.persistence.AccountPropertiesDao
import com.msaber.openapiapp.persistence.AppDatabase
import com.msaber.openapiapp.persistence.AppDatabase.Companion.DATABASE_NAME
import com.msaber.openapiapp.util.LiveDataCallAdapterFactory
import com.msaber.openapiapp.util.constant.Constants
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class AppModule{


    @Singleton
    @Provides
    fun provideSharePreference(application: Application): SharedPreferences{
        return application.getSharedPreferences(PreferenceKeys.APP_PREFERENCES,Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideSharePreferenceEditor(sharedPreferences: SharedPreferences): SharedPreferences.Editor{
        return sharedPreferences.edit()
    }

    @Singleton
    @Provides
    fun provideGsonBuilder(): Gson{
        return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder(gson: Gson): Retrofit.Builder{
        return Retrofit.Builder().baseUrl(Constants.BASE_URL).addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration() // get correct db version if schema changed
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthTokenDao(db: AppDatabase): AuthTokenDao {
        return db.getAuthTokenDao()
    }

    @Singleton
    @Provides
    fun provideAccountPropertiesDao(db: AppDatabase): AccountPropertiesDao {
        return db.getAccountProperties()
    }

    @Singleton
    @Provides
    fun provideRequestOptions(): RequestOptions {
        return RequestOptions
            .placeholderOf(R.drawable.default_image)
            .error(R.drawable.default_image)
    }

    @Singleton
    @Provides
    fun provideGlideInstance(application: Application, requestOptions: RequestOptions): RequestManager {
        return Glide.with(application)
            .setDefaultRequestOptions(requestOptions)
    }

}