package co.uk.basedapps.domain_wireguard.core.init

import android.content.Context
import android.content.Intent
import com.wireguard.android.backend.GoBackend

fun getVpnPermissionRequest(context: Context): Intent? {
  return GoBackend.VpnService.prepare(context)
}
