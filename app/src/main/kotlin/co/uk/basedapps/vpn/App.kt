package co.uk.basedapps.vpn

import android.app.Application
import co.sentinel.vpn.based.vpn.VpnInitializer
import dagger.hilt.android.HiltAndroidApp
import io.norselabs.vpn.common_logger.logger.FileLogTree
import io.norselabs.vpn.common_logger.logger.NonFatalReportTree
import io.norselabs.vpn.core_vpn.user.UserInitializer
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

  @Inject
  lateinit var vpnInitializer: VpnInitializer

  @Inject
  lateinit var fileLogTree: FileLogTree

  @Inject
  lateinit var nonFatalReportTree: NonFatalReportTree

  @Inject
  lateinit var userInitializer: UserInitializer

  override fun onCreate() {
    super.onCreate()
    setupTimber()
    vpnInitializer.setupVPN()
    userInitializer.enroll()
  }

  private fun setupTimber() {
    Timber.plant(
      Timber.DebugTree(),
      fileLogTree,
      nonFatalReportTree,
    )
  }
}
