package co.uk.basedapps.vpn.di

import co.sentinel.vpn.based.app_config.AppConfig
import co.uk.basedapps.vpn.BuildConfig
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
  fun provideAppConfig(): AppConfig = object : AppConfig {
    override fun getAppVersion() = BuildConfig.VERSION_NAME
    override fun getPackage() = "co.uk.basedapps.vpn"
    override fun getBaseUrl() = BuildConfig.API_URL
    override fun getBasedAppVersion(): Long = 1
    override fun getBasedApiVersion(): Long = 2
    override fun getAppToken(): String =
      "PyAouXBv113edAawcaZURFlbr97gIS2UPJRvBOdbjerY3KPVtE8EXyvNNXjX2VMT2U8YvPqvv8f4jog3GQCrqTC0qoVPVPenkr2fJy1PVahMojKJbvUKEqLo3hqt7wW8"
  }
}
