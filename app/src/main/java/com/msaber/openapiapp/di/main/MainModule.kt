package com.msaber.openapiapp.di.main

import com.msaber.openapiapp.api.main.OpenApiMainService
import com.msaber.openapiapp.persistence.AccountPropertiesDao
import com.msaber.openapiapp.persistence.AppDatabase
import com.msaber.openapiapp.persistence.BlogPostDao
import com.msaber.openapiapp.repository.main.AccountRepository
import com.msaber.openapiapp.repository.main.BlogRepository
import com.msaber.openapiapp.repository.main.CreateBlogRepository
import com.msaber.openapiapp.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder.build().create(OpenApiMainService::class.java)
    }

    @MainScope
    @Provides
        fun provideAccountRepository(
        openApiMainService: OpenApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {

        return AccountRepository(openApiMainService, accountPropertiesDao, sessionManager)
    }



    @MainScope
    @Provides
    fun provideBlogPostDao(db:AppDatabase):BlogPostDao{
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(openApiMainService, blogPostDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(openApiMainService, blogPostDao, sessionManager)
    }
}