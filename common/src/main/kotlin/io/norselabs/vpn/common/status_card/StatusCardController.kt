package io.norselabs.vpn.common.status_card

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatusCardController(
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {

  private val _state = MutableStateFlow<StatusCardState?>(null)
  val state: StateFlow<StatusCardState?> = _state.asStateFlow()

  private val _retryClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val retryClicks: SharedFlow<Unit> = _retryClicks.asSharedFlow()

  private var hideJob: Job? = null

  fun show(state: StatusCardState, hideAfter: Duration? = null) {
    hideJob?.cancel()
    _state.value = state
    if (hideAfter != null) {
      hideJob = scope.launch {
        delay(hideAfter)
        _state.value = null
      }
    }
  }

  fun showLoading(
    title: String,
    subtitle: String? = null,
    hideAfter: Duration? = null,
  ) {
    show(StatusCardState.Loading(title, subtitle), hideAfter)
  }

  fun showSuccess(
    title: String,
    subtitle: String? = null,
    hideAfter: Duration? = 3.seconds,
  ) {
    show(StatusCardState.Success(title, subtitle), hideAfter)
  }

  fun showInfo(
    title: String,
    subtitle: String? = null,
    hideAfter: Duration? = 3.seconds,
  ) {
    show(StatusCardState.Info(title, subtitle), hideAfter)
  }

  fun showError(
    title: String,
    subtitle: String? = null,
    retryable: Boolean = false,
    hideAfter: Duration? = if (retryable) null else 3.seconds,
  ) {
    show(StatusCardState.Error(title, subtitle, retryable), hideAfter)
  }

  fun hide() {
    hideJob?.cancel()
    _state.value = null
  }

  fun onRetryClick() {
    _retryClicks.tryEmit(Unit)
  }
}
