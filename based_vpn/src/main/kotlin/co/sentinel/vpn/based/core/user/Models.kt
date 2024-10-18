package co.sentinel.vpn.based.core.user

enum class UserStatus {
  Init,
  Enrolled,
  NotEnrolled,
  Banned,
  VersionOutdated,
  Failed,
}

data class Token(
  val id: Int,
  val token: String,
  val isBanned: Boolean,
  val isEnrolled: Boolean,
)
