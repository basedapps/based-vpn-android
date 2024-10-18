package co.sentinel.vpn.based.user

import arrow.core.Either
import arrow.core.flatMap
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class UserInitializer(
  private val scope: CoroutineScope,
  private val interactor: UserInitializerInteractor,
) {

  private val _status = MutableStateFlow(UserStatus.Init)
  val status: StateFlow<UserStatus>
    get() = _status

  private var enrolmentAttempt = 0

  private var job: Job? = null

  fun enroll() {
    job?.cancel()
    _status.value = UserStatus.Init
    enrolmentAttempt = 0

    job = scope.launch {
      isUpdateRequired()
        .flatMap { checkToken() }
        .flatMap { checkEnrollment() }
        .onLeft { status -> _status.value = status }
        .onRight { _status.value = UserStatus.Enrolled }
    }
  }

  private suspend fun isUpdateRequired(): Either<UserStatus, Unit> {
    Timber.tag(TAG).d("Check version")
    return interactor.checkVersion()
      .flatMap { isFine ->
        when {
          isFine -> Either.Right(Unit)
          else -> Either.Left(UserStatus.VersionOutdated)
        }
      }
      .mapLeft { UserStatus.VersionOutdated }
  }

  private suspend fun checkToken(): Either<UserStatus, Unit> {
    Timber.tag(TAG).d("Check token")
    return when {
      interactor.getToken().isEmpty() -> getToken().map { }
      else -> Either.Right(Unit)
    }
  }

  private suspend fun getToken(): Either<UserStatus, Token> {
    Timber.tag(TAG).d("Get token")
    return interactor.registerDevice()
      .onRight { model ->
        Timber.tag(TAG).d("Token has been updated")
        interactor.storeToken(model.token)
      }
      .mapLeft { UserStatus.Failed }
  }

  private suspend fun checkEnrollment(): Either<UserStatus, Unit> {
    enrolmentAttempt++
    Timber.tag(TAG).d("Try enroll. Attempt $enrolmentAttempt")
    return interactor.getSession()
      .fold(
        ifLeft = { exception ->
          val code = (exception as? HttpException)?.response()?.code()
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
            delay(DELAY_SEC.seconds)
            checkEnrollment()
          }

          else -> Either.Left(UserStatus.NotEnrolled)
        }
      }
  }

  companion object {
    const val TAG = "UserInitializer"
    const val MAX_ATTEMPTS = 20
    const val DELAY_SEC = 5
  }
}

interface UserInitializerInteractor {

  fun storeToken(token: String)
  fun getToken(): String

  suspend fun checkVersion(): Either<Exception, Boolean>
  suspend fun registerDevice(): Either<Exception, Token>
  suspend fun getSession(): Either<Exception, Token>
}

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
