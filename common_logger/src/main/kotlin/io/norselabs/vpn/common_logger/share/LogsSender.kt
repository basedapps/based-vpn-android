package io.norselabs.vpn.common_logger.share

import android.app.Activity
import io.norselabs.vpn.common_logger.logger.FileLogTree
import java.lang.ref.WeakReference

class LogsSender(
  private val appId: String,
  private val fileLogTree: FileLogTree,
) {

  private var weakActivity = WeakReference<Activity?>(null)

  fun init(activity: Activity) {
    weakActivity = WeakReference(activity)
  }

  fun shareLogs() {
    val activity = weakActivity.get() ?: return
    val file = fileLogTree.getLogsFile() ?: return
    ChooserIntent.start(activity, appId, file)
  }
}
