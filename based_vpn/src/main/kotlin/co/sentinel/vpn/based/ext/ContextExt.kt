package co.sentinel.vpn.based.ext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.goToGooglePlay() {
  try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
  } catch (e: ActivityNotFoundException) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
  }
}

fun Context.openWeb(url: String) {
  val uri = url.parseUrl() ?: return
  val intent = Intent(Intent.ACTION_VIEW, uri)
  startActivity(intent)
}
