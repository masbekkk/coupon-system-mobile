package com.masbek.couponsystem.di

import com.masbek.couponsystem.data.api.ApiService
import com.masbek.couponsystem.data.repository.*
import com.masbek.couponsystem.util.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(api: ApiService, sessionManager: SessionManager): AuthRepository {
        return AuthRepository(api, sessionManager)
    }

    @Provides
    @Singleton
    fun provideDashboardRepository(api: ApiService): DashboardRepository {
        return DashboardRepository(api)
    }

    @Provides
    @Singleton
    fun provideProjectRepository(api: ApiService): ProjectRepository {
        return ProjectRepository(api)
    }

    @Provides
    @Singleton
    fun provideBatchRepository(api: ApiService): BatchRepository {
        return BatchRepository(api)
    }

    @Provides
    @Singleton
    fun provideCouponRepository(api: ApiService): CouponRepository {
        return CouponRepository(api)
    }
}
