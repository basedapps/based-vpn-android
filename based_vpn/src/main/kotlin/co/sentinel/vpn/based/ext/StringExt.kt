package co.sentinel.vpn.based.ext

import android.net.Uri

fun String.parseUrl(): Uri? = try {
  Uri.parse(this)
} catch (e: Throwable) {
  null
}
