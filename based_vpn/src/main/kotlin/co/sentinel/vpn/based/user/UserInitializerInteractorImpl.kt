package co.sentinel.vpn.based.user

import arrow.core.Either
import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.network.model.DataObj
import co.sentinel.vpn.based.network.model.TokenModel
import co.sentinel.vpn.based.network.repository.BasedRepository
import co.sentinel.vpn.based.storage.BasedStorage

class UserInitializerInteractorImpl(
  private val repository: BasedRepository,
  private val storage: BasedStorage,
  private val config: AppConfig,
) : UserInitializerInteractor {

  override fun storeToken(token: String) {
    storage.storeToken(token)
  }

  override fun getToken(): String {
    return storage.getToken()
  }

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
}
