package co.sentinel.vpn.based.core.user

import arrow.core.Either

interface UserInitializerInteractor {

  fun storeToken(token: String)
  fun getToken(): String

  suspend fun checkVersion(): Either<Exception, Boolean>
  suspend fun registerDevice(): Either<Exception, Token>
  suspend fun getSession(): Either<Exception, Token>

  fun parseHttpCode(exception: Exception): Int?
}
