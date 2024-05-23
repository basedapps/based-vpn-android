package co.uk.basedapps.vpn.vpn

import android.util.Base64
import co.sentinel.vpn.v2ray.model.V2RayVpnProfile
import co.sentinel.vpn.wireguard.model.WireguardVpnProfile

fun decodeWireguardVpnProfile(payload: String): WireguardVpnProfile? {
  return try {
    Base64.decode(payload, Base64.DEFAULT).let { bytes ->
      val address =
        "${bytes[0].toUByte()}.${bytes[1].toUByte()}.${bytes[2].toUByte()}.${bytes[3].toUByte()}/32"
      val port = bytesToUnsignedShort(bytes[24], bytes[25], bigEndian = true)
      val host =
        "${bytes[20].toUByte()}.${bytes[21].toUByte()}.${bytes[22].toUByte()}.${bytes[23].toUByte()}"
      val peerEndpoint = "$host:$port"
      val pubKeyBytes = bytes.copyOfRange(26, 58)

      WireguardVpnProfile(
        address = address,
        host = host,
        listenPort = "$port",
        peerEndpoint = peerEndpoint,
        peerPubKeyBytes = pubKeyBytes,
      )
    }
  } catch (e: Exception) {
    null
  }
}

fun decodeV2RayVpnProfile(
  payload: String,
  uid: String,
): V2RayVpnProfile? {
  return try {
    Base64.decode(payload, Base64.DEFAULT).let { bytes ->
      if (bytes.size != 7) return null
      val address = "${bytes[0].toUByte()}.${bytes[1].toUByte()}.${bytes[2].toUByte()}.${bytes[3].toUByte()}"
      val port = bytesToUnsignedShort(bytes[4], bytes[5], bigEndian = true)
      val hex = String.format("0x%02x", (bytes[6].toInt() and 0xFF))
      val transport: String = when (hex) {
        "0x01" -> "tcp"
        "0x02" -> "mkcp"
        "0x03" -> "websocket"
        "0x04" -> "http"
        "0x05" -> "domainsocket"
        "0x06" -> "quic"
        "0x07" -> "gun"
        "0x08" -> "grpc"
        else -> ""
      }

      V2RayVpnProfile(
        uid = uid,
        address = address,
        listenPort = "$port",
        transport = transport,
      )
    }
  } catch (e: Exception) {
    null
  }
}

private fun bytesToUnsignedShort(
  byte1: Byte,
  byte2: Byte,
  bigEndian: Boolean,
): Int {
  if (bigEndian) {
    return (((byte1.toInt() and 255) shl 8) or (byte2.toInt() and 255))
  }
  return (((byte2.toInt() and 255) shl 8) or (byte1.toInt() and 255))
}
