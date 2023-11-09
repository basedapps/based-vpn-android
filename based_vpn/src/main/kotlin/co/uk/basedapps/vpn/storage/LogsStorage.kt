package co.uk.basedapps.vpn.storage

import co.uk.basedapps.vpn.common.BaseError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogsStorage
@Inject constructor() {

  private val logList = CopyOnWriteArrayList<LogEntity>()
  private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z", Locale.getDefault())

  fun writeToLog(error: BaseError) {
    logList.add(
      LogEntity(
        time = System.currentTimeMillis(),
        message = error.message,
      ),
    )
  }

  fun getLogs(): String = logList
    .joinToString(separator = "\n\n") {
      "${dateFormatter.format(Date(it.time))} : ${it.message}"
    }
}
