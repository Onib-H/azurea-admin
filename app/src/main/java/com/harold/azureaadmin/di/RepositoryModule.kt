package com.harold.azureaadmin.di

import android.content.Context
import com.harold.azureaadmin.data.remote.AdminApiService
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAdminRepository(
        @ApplicationContext context: Context,
        api: AdminApiService
    ): AdminRepository {
        return AdminRepository(context, api)
    }
}
