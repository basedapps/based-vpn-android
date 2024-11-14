package io.norselabs.vpn.core_vpn.user

import arrow.core.Either

interface UserInitializerInteractor {

  fun storeUserData(token: String, userId: String)
  fun getToken(): String

  suspend fun checkVersion(): Either<Exception, Boolean>
  suspend fun registerDevice(): Either<Exception, Token>
  suspend fun getSession(): Either<Exception, Token>

  fun parseHttpCode(exception: Exception): Int?
}
