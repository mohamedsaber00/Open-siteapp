package com.msaber.openapiapp.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msaber.openapiapp.model.AccountProperties
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.model.BlogPost

@Database(entities = [AuthToken::class,AccountProperties::class, BlogPost::class],version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountProperties(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    companion object{
        const val  DATABASE_NAME = "app_db"
    }


}