package co.uk.basedapps.vpn.di

import co.uk.basedapps.vpn.BuildConfig
import co.uk.basedapps.vpn.common.provider.AppDetailsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

  @Provides
  @Singleton
  fun provideAppDetailsProvider(): AppDetailsProvider =
    object : AppDetailsProvider {
      override fun getAppVersion() = BuildConfig.VERSION_NAME
      override fun getPackage() = "co.uk.basedapps.vpn"
      override fun getBaseUrl() = BuildConfig.API_URL
    }
}
