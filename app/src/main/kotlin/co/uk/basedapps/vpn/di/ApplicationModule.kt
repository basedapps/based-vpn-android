package co.uk.basedapps.vpn.di

import android.content.Context
import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.viewModel.split_tunneling.NetAppsProvider
import co.sentinel.vpn.based.viewModel.split_tunneling.NetworkApp
import co.uk.basedapps.vpn.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.common_logger.logger.FileLogTree
import io.norselabs.vpn.common_logger.logger.NonFatalReportTree
import io.norselabs.vpn.common_net_apps.AppManagerUtil
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

  @Provides
  @Singleton
  fun provideFileLogTree(@ApplicationContext context: Context): FileLogTree {
    return FileLogTree(context)
  }

  @Provides
  @Singleton
  fun provideNonFatalReportTree(@ApplicationContext context: Context): NonFatalReportTree {

    val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    return NonFatalReportTree(
      context = context,
      log = { crashlytics.log(it) },
      recordException = { crashlytics.recordException(it) },
      setCustomKeys = {
        crashlytics.setCustomKeys {
          it.forEach { (key, value) -> key(key, value) }
        }
      },
    )
  }

  @Provides
  @Singleton
  fun provideNetAppsProvider(
    @ApplicationContext context: Context,
  ): NetAppsProvider {
    return object : NetAppsProvider {
      override suspend fun getNetApps(): List<NetworkApp> {
        return withContext(Dispatchers.IO) {
          AppManagerUtil.getNetworkAppList(context)
            .map {
              NetworkApp(
                appName = it.appName,
                packageName = it.packageName,
                appIcon = it.appIcon,
                isSystemApp = it.isSystemApp,
                isChecked = false,
              )
            }
        }
      }
    }
  }
}
