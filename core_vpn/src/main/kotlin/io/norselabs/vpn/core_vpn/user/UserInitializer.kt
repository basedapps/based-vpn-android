package io.norselabs.vpn.core_vpn.user

import arrow.core.Either
import arrow.core.flatMap
import io.norselabs.vpn.common.utils.VersionComparator
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.sdk.common.SdkError
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.services.device.TokenModel
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class UserInitializer(
  private val scope: CoroutineScope,
  private val coreStorage: CoreStorage,
  private val dvpn: DVPNClient,
  private val appVersion: String,
) {

  private val _status = MutableStateFlow(UserStatus.Determining)
  val status: StateFlow<UserStatus>
    get() = _status

  private var enrolmentAttempt = 0

  private var job: Job? = null

  fun enroll() {
    job?.cancel()
    _status.value = UserStatus.Determining
    enrolmentAttempt = 0

    job = scope.launch {
      isUpdateRequired()
        .flatMap { checkToken() }
        .onRight { _status.value = UserStatus.HasToken }
        .flatMap { checkEnrollment() }
        .onLeft { status -> _status.value = status }
        .onRight { _status.value = UserStatus.Enrolled }
    }
  }

  fun hasDeviceToken(): Flow<Boolean> {
    return status.map { it.order > UserStatus.HasToken.order }
  }

  suspend fun getDeviceToken(): String {
    hasDeviceToken().firstOrNull { it }
    return coreStorage.getToken()
  }

  private suspend fun isUpdateRequired(): Either<UserStatus, Unit> {
    Timber.tag(TAG).d("Check version")
    val minVersion = dvpn.getVersion().getOrNull() ?: "0.0.0"
    val isFine = VersionComparator.compare(appVersion, minVersion) != -1
    return when {
      isFine -> Either.Right(Unit)
      else -> Either.Left(UserStatus.VersionOutdated)
    }
  }

  private suspend fun checkToken(): Either<UserStatus, Unit> {
    Timber.tag(TAG).d("Check token")
    return when {
      coreStorage.getToken().isEmpty() -> getToken().map { }
      else -> Either.Right(Unit)
    }
  }

  private suspend fun getToken(): Either<UserStatus, Token> {
    Timber.tag(TAG).d("Get token")
    return dvpn.registerDevice()
      .map(::parseToken)
      .onRight { model ->
        Timber.tag(TAG).d("Token has been updated")
        coreStorage.setToken(model.token)
        coreStorage.setUserId(model.id)
      }
      .mapLeft { UserStatus.Failed }
  }

  private suspend fun checkEnrollment(): Either<UserStatus, Unit> {
    enrolmentAttempt++
    Timber.tag(TAG).d("Try enroll. Attempt $enrolmentAttempt")
    return dvpn.getSession()
      .map(::parseToken)
      .fold(
        ifLeft = { exception ->
          val code = (exception as? SdkError.HttpError)?.code
          when (code) {
            401 -> {
              Timber.tag(TAG).d("Token expired")
              getToken()
            }

            else -> Either.Left(UserStatus.Failed)
          }
        },
        ifRight = { Either.Right(it) },
      )
      .flatMap { session ->
        when {
          session.isBanned -> Either.Left(UserStatus.Banned)
          session.isEnrolled -> Either.Right(Unit)
          enrolmentAttempt < MAX_ATTEMPTS -> {
            _status.value = UserStatus.Enrolling
            delay(DELAY_SEC.seconds)
            checkEnrollment()
          }

          else -> Either.Left(UserStatus.NotEnrolled)
        }
      }
  }

  private fun parseToken(data: TokenModel): Token {
    return Token(
      id = data.id,
      token = data.token,
      isBanned = data.isBanned,
      isEnrolled = data.isEnrolled,
    )
  }

  companion object {
    const val TAG = "UserInitializer"
    const val MAX_ATTEMPTS = 20
    const val DELAY_SEC = 5
  }
}
