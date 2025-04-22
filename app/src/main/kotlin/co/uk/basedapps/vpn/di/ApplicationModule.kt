package co.uk.basedapps.vpn.di

import android.content.Context
import co.uk.basedapps.vpn.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.based.app_config.AppConfig
import io.norselabs.vpn.based.network.DnsRequests
import io.norselabs.vpn.based.viewModel.split_tunneling.NetAppsProvider
import io.norselabs.vpn.based.viewModel.split_tunneling.NetworkApp
import io.norselabs.vpn.common_logger.logger.FileLogTree
import io.norselabs.vpn.common_logger.logger.NonFatalReportTree
import io.norselabs.vpn.common_net_apps.AppManagerUtil
import io.norselabs.vpn.common_purchases.PurchasesManager
import io.norselabs.vpn.common_purchases.PurchasesManagerStub
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

  @Provides
  @Singleton
  fun provideAppConfig(): AppConfig = object : AppConfig {
    override fun getAppId(): String = BuildConfig.APPLICATION_ID
    override fun getAppVersion() = BuildConfig.VERSION_NAME
    override fun getPackage() = "co.uk.basedapps.vpn"
    override fun getBaseUrl() = "https://api.dvpnsdk.com"
    override fun getAppToken(): String = "d03xozoa1yiiojofx19fs3mhbmyy2lre"
  }

  @Provides
  @Singleton
  fun provideFileLogTree(
    @ApplicationContext context: Context,
    appConfig: AppConfig,
  ): FileLogTree {
    return FileLogTree(
      context = context,
      excluded = listOf(appConfig.getBaseUrl().replace("https://", "")),
    )
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

  @Provides
  @Singleton
  fun provideDnsRequests(): DnsRequests {
    return object : DnsRequests {
      override val version: String = "update.independentdvpn.com"
    }
  }

  @Provides
  @Singleton
  fun providePurchasesManager(): PurchasesManager {
    return PurchasesManagerStub()
  }
}
