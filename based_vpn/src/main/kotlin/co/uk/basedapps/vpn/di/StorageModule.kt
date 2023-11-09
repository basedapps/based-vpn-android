package co.uk.basedapps.vpn.di

import android.content.Context
import android.content.SharedPreferences
import co.uk.basedapps.vpn.common.provider.AppDetailsProvider
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {

  @Provides
  @Singleton
  fun provideSharedPreferences(
    @ApplicationContext context: Context,
    provider: AppDetailsProvider,
  ): SharedPreferences =
    context.getSharedPreferences(provider.getPackage(), Context.MODE_PRIVATE)

  @Provides
  @Singleton
  fun provideGson() = Gson()
}
