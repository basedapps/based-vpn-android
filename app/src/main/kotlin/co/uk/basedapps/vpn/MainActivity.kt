package co.uk.basedapps.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import cafe.adriel.voyager.navigator.Navigator
import co.uk.basedapps.vpn.ui.screens.dashboard.DashboardScreen
import co.uk.basedapps.vpn.ui.theme.BasedVPNTheme
import dagger.hilt.android.AndroidEntryPoint
import io.norselabs.vpn.common_logger.share.LogsSender
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  @Inject
  lateinit var logsSender: LogsSender

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logsSender.init(this)
    setFullScreen()
    setContent {
      BasedVPNTheme {
        Navigator(DashboardScreen())
      }
    }
  }
}

fun ComponentActivity.setFullScreen() {
  WindowCompat.setDecorFitsSystemWindows(window, false)
}
