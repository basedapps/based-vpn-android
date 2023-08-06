package co.uk.basedapps.domain.extension

fun bytesToUnsignedShort(byte1: Byte, byte2: Byte, bigEndian: Boolean): Int {
  if (bigEndian) {
    return (((byte1.toInt() and 255) shl 8) or (byte2.toInt() and 255))
  }
  return (((byte2.toInt() and 255) shl 8) or (byte1.toInt() and 255))
}
