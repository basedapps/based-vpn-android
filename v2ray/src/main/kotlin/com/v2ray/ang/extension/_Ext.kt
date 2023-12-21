package com.v2ray.ang.extension

import android.content.Context
import android.os.Build
import android.util.Log
import com.v2ray.ang.AppConfig
import java.net.URI
import java.net.URLConnection
import org.json.JSONObject

/**
 * Some extensions
 */

fun Context.toast(message: Int) {
  Log.d(AppConfig.ANG_LOGS_TAG, "Toast msg: $message")
  // Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun JSONObject.putOpt(pair: Pair<String, Any>) = putOpt(pair.first, pair.second)
fun JSONObject.putOpt(pairs: Map<String, Any>) = pairs.forEach { putOpt(it.key to it.value) }

const val threshold = 1000
const val divisor = 1024F

fun Long.toSpeedString() = toTrafficString() + "/s"

fun Long.toTrafficString(): String {
  if (this == 0L) {
    return "\t\t\t0\t  B"
  }

  if (this < threshold) {
    return "${this.toFloat().toShortString()}\t  B"
  }

  val kib = this / divisor
  if (kib < threshold) {
    return "${kib.toShortString()}\t KB"
  }

  val mib = kib / divisor
  if (mib < threshold) {
    return "${mib.toShortString()}\t MB"
  }

  val gib = mib / divisor
  if (gib < threshold) {
    return "${gib.toShortString()}\t GB"
  }

  val tib = gib / divisor
  if (tib < threshold) {
    return "${tib.toShortString()}\t TB"
  }

  val pib = tib / divisor
  if (pib < threshold) {
    return "${pib.toShortString()}\t PB"
  }

  return "∞"
}

private fun Float.toShortString(): String {
  val s = "%.2f".format(this)
  if (s.length <= 4) {
    return s
  }
  return s.substring(0, 4).removeSuffix(".")
}

val URLConnection.responseLength: Long
  get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) contentLengthLong else contentLength.toLong()

val URI.idnHost: String
  get() = (host!!).replace("[", "").replace("]", "")