package co.uk.basedapps.vpn.network

import co.uk.basedapps.domain.functional.Either

typealias NetResult<T> = Either<Exception, T>

suspend fun <T> execute(
  method: suspend () -> T,
): NetResult<T> {
  return try {
    Either.Right(method.invoke())
  } catch (e: Exception) {
    Either.Left(e)
  }
}
