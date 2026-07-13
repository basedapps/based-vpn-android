package io.norselabs.vpn.core_vpn.vpn.utils

import android.util.Base64
import io.norselabs.vpn.core_vpn.vpn.Credentials
import io.norselabs.vpn.v2ray.model.VmessTransport
import io.norselabs.vpn.v2ray.model.VpnProfile

object ProfileDecoder {

  fun decode(credentials: Credentials): VpnProfile? {
    return when (credentials) {
      is Credentials.Wireguard -> decodeWireguard(
        privateKey = credentials.privateKey,
        payload = credentials.payload,
      )

      is Credentials.V2Ray -> decodeVmess(
        payload = credentials.payload,
        uid = credentials.uid,
      )
    }
  }

  private fun decodeVmess(
    payload: String,
    uid: String,
  ): VpnProfile? {
    return try {
      Base64.decode(payload, Base64.DEFAULT).let { bytes ->
        if (bytes.size != 7) return null
        val address = "${bytes[0].toUByte()}.${bytes[1].toUByte()}.${bytes[2].toUByte()}.${bytes[3].toUByte()}"
        val port = bytesToUnsignedShort(bytes[4], bytes[5]).toString()
        // Unsupported types return null instead of silently falling back to TCP:
        // 0x02 mkcp — UDP: the TLS probe can't reach it and cert pinning needs the node cert SHA-256 from the backend; enable once the backend sends it
        // 0x05 domainsocket — removed from Xray-core
        // 0x06 quic — removed from Xray-core 26
        // 0x07 gun — obsolete gRPC alias, not sent by the backend
        val transport: VmessTransport = when (bytes[6].toInt() and 0xFF) {
          0x01 -> VmessTransport.TCP
          0x03 -> VmessTransport.WS
          0x04 -> VmessTransport.H2
          0x08 -> VmessTransport.GRPC
          else -> return null
        }

        VpnProfile.Vmess(
          uid = uid,
          address = address,
          listenPort = port,
          transport = transport,
        )
      }
    } catch (e: Exception) {
      null
    }
  }

  private fun decodeWireguard(
    privateKey: String,
    payload: String,
  ): VpnProfile? {
    return try {
      Base64.decode(payload, Base64.DEFAULT).let { bytes ->
        val address = "${bytes[0].toUByte()}.${bytes[1].toUByte()}.${bytes[2].toUByte()}.${bytes[3].toUByte()}/32"
        val port = bytesToUnsignedShort(bytes[24], bytes[25]).toString()
        val host = "${bytes[20].toUByte()}.${bytes[21].toUByte()}.${bytes[22].toUByte()}.${bytes[23].toUByte()}"
        val pubKeyBytes = bytes.copyOfRange(26, 58)
        val peerPubKeyBase64 = String(Base64.encode(pubKeyBytes, Base64.DEFAULT)).trim()

        VpnProfile.Wireguard(
          privateKey = privateKey,
          address = address,
          host = host,
          listenPort = port,
          peerPubKeyBase64 = peerPubKeyBase64,
        )
      }
    } catch (e: Exception) {
      null
    }
  }

  private fun bytesToUnsignedShort(
    byte1: Byte,
    byte2: Byte,
  ): Int {
    return (((byte1.toInt() and 255) shl 8) or (byte2.toInt() and 255))
  }
}
