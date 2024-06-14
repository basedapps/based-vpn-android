package co.uk.basedapps.vpn

import android.app.Application
import co.sentinel.vpn.based.vpn.VpnInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

  override fun onCreate() {
    super.onCreate()
    setupTimber()
    VpnInitializer.setupVPN(this)
  }

  private fun setupTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}
