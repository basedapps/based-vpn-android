package io.norselabs.logging.logger

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import io.norselabs.logging.NonFatalException
import timber.log.Timber

class NonFatalReportTree(context: Context) : Timber.Tree() {

  private val isMainProcess = context.isMainProcess()
  private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

  init {
    if (isMainProcess) {
      crashlytics.setCustomKeys {
        key("processor-arch", System.getProperty("os.arch").orEmpty())
      }
    }
  }

  override fun log(
    priority: Int,
    tag: String?,
    message: String,
    t: Throwable?,
  ) {
    if (!isMainProcess) return
    if (priority != Log.ERROR) return
    val nonFatalException = t as? NonFatalException ?: return

    nonFatalException.message?.takeIfNotBlank()?.let { crashlytics.log(it) }
    val actualException = nonFatalException.cause ?: nonFatalException
    crashlytics.recordException(actualException)
  }

  private fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

  private fun Context.isMainProcess(): Boolean {
    return packageName.equals(getProcessName())
  }

  private fun Context.getProcessName(): String? {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    manager?.runningAppProcesses?.forEach { process ->
      if (process.pid == Process.myPid()) {
        return process.processName
      }
    }
    return null
  }
}
