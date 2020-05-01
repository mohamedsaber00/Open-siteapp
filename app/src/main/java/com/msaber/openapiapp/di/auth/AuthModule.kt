package com.msaber.openapiapp.di.auth

import android.content.SharedPreferences
import com.msaber.openapiapp.persistence.AuthTokenDao
import com.msaber.openapiapp.api.auth.OpenApiAuthService
import com.msaber.openapiapp.persistence.AccountPropertiesDao
import com.msaber.openapiapp.repository.auth.AuthRepository
import com.msaber.openapiapp.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule{

    // TEMPORARY
    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): OpenApiAuthService{
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService,
        sharedPreferences: SharedPreferences,
        sharedPreferencesEditor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,sharedPreferences,sharedPreferencesEditor
        )
    }

}