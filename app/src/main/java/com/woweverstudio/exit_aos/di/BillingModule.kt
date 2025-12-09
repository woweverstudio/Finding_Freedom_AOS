package com.woweverstudio.exit_aos.di

import android.content.Context
import com.woweverstudio.exit_aos.data.billing.BillingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    
    @Provides
    @Singleton
    fun provideBillingService(
        @ApplicationContext context: Context
    ): BillingService {
        return BillingService(context).also {
            it.initialize()
        }
    }
}

