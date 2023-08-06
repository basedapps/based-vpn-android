package co.uk.basedapps.vpn

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.dev7.lib.v2ray.V2rayController
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

  override fun onCreate() {
    super.onCreate()
    setupTimber()
  }

  private fun setupTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}
