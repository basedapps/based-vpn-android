package co.uk.basedapps.vpn

import android.app.Application
import co.sentinel.vpn.based.vpn.VpnInitializer
import dagger.hilt.android.HiltAndroidApp
import io.norselabs.logging.logger.FileLogTree
import io.norselabs.logging.logger.NonFatalReportTree
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

  override fun onCreate() {
    super.onCreate()
    setupTimber()
    vpnInitializer.setupVPN()
  }

  private fun setupTimber() {
    Timber.plant(
      Timber.DebugTree(),
      fileLogTree,
      nonFatalReportTree,
    )
  }
}
