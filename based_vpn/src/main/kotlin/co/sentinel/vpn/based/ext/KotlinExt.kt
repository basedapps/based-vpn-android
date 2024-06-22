package co.sentinel.vpn.based.ext

fun <T> T.oneOf(vararg list: T): Boolean {
  return this in list
}
