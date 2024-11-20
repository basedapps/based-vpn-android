package co.sentinel.vpn.based.core_impl.user

import arrow.core.Either
import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.network.model.DataObj
import co.sentinel.vpn.based.network.model.TokenModel
import co.sentinel.vpn.based.network.repository.AppRepository
import io.norselabs.vpn.core_vpn.user.Token
import io.norselabs.vpn.core_vpn.user.UserInitializerInteractor
import retrofit2.HttpException

class UserInitializerInteractorImpl(
  private val repository: AppRepository,
  private val config: AppConfig,
) : UserInitializerInteractor {

  override suspend fun checkVersion(): Either<Exception, Boolean> {
    return repository.getVersion()
      .map { response ->
        val versions = response.data
        versions.appVersion <= config.getBasedAppVersion() &&
          versions.apiVersion <= config.getBasedApiVersion()
      }
  }

  override suspend fun registerDevice(): Either<Exception, Token> {
    return repository.registerDevice().map(::parseToken)
  }

  override suspend fun getSession(): Either<Exception, Token> {
    return repository.getSession().map(::parseToken)
  }

  private fun parseToken(model: DataObj<TokenModel>): Token {
    val data = model.data
    return Token(
      id = data.id,
      token = data.token,
      isBanned = data.isBanned,
      isEnrolled = data.isEnrolled,
    )
  }

  override fun parseHttpCode(exception: Exception): Int? {
    return (exception as? HttpException)?.response()?.code()
  }
}
