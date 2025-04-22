package co.uk.basedapps.vpn

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.norselabs.vpn.common_logger.logger.FileLogTree
import io.norselabs.vpn.common_logger.logger.NonFatalReportTree
import io.norselabs.vpn.core_vpn.connectivity.NetworkStateMonitor
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.user.UserInitializer
import io.norselabs.vpn.core_vpn.vpn.Protocol
import io.norselabs.vpn.core_vpn.vpn.dns.DnsConfigurator
import io.norselabs.vpn.v2ray.control.V2RayInitializer
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

  @Inject
  lateinit var fileLogTree: FileLogTree

  @Inject
  lateinit var nonFatalReportTree: NonFatalReportTree

  @Inject
  lateinit var userInitializer: UserInitializer

  @Inject
  lateinit var networkMonitor: NetworkStateMonitor

  @Inject
  lateinit var coreStorage: CoreStorage

  @Inject
  lateinit var dnsConfigurator: DnsConfigurator

  override fun onCreate() {
    super.onCreate()
    setupTimber()
    setDefaultProtocol()
    V2RayInitializer.init(applicationContext)
    networkMonitor.startMonitoring()
    dnsConfigurator.init()
    userInitializer.enroll()
  }

  private fun setupTimber() {
    Timber.plant(
      Timber.DebugTree(),
      fileLogTree,
      nonFatalReportTree,
    )
  }

  private fun setDefaultProtocol() {
    val wasSelected = coreStorage.wasVpnProtocolSelected()
    if (!wasSelected) {
      coreStorage.setVpnProtocol(Protocol.V2RAY)
    }
  }
}
