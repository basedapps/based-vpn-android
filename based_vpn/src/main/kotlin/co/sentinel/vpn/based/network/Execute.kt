package co.sentinel.vpn.based.network

import arrow.core.Either
import timber.log.Timber

typealias NetResult<T> = Either<Exception, T>

suspend fun <T> execute(method: suspend () -> T): NetResult<T> {
  return try {
    Either.Right(method.invoke())
  } catch (e: Exception) {
    Timber.e(e)
    Either.Left(e)
  }
}
