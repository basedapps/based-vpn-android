package io.norselabs.vpn.common_compose.prompt_sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * How a prompt shown via [PromptHostState.show] was resolved.
 * Every way the prompt can go away maps to exactly one value, so an
 * exhaustive `when` can't miss an exit path (unlike listening for
 * navigator/dismiss callbacks by hand).
 */
enum class PromptResult {
  /** The confirm button was tapped. */
  Confirmed,

  /** The dismiss ("not now") button was tapped. */
  Dismissed,

  /** Closed without a button tap: swipe, scrim tap or back press. */
  Closed,
}

@Immutable
class PromptData(
  val title: String,
  val description: String? = null,
  val confirmLabel: String,
  val dismissLabel: String? = null,
)

/**
 * Imperative controller for a prompt host — the SnackbarHostState pattern:
 * call [show] from a coroutine (e.g. an effect handler) and get the user's
 * answer back as a [PromptResult]. No visibility flags leak into screen or
 * ViewModel state. Concurrent calls queue up one at a time.
 *
 * The presentation is chosen by the host composable the state is wired to —
 * [PromptSheetHost] (bottom sheet) or [PromptDialogHost] (alert dialog).
 * One state drives exactly one host; a screen that needs both forms holds
 * two states.
 */
@Stable
class PromptHostState {

  internal var current: PromptRequest? by mutableStateOf(null)
    private set

  private val mutex = Mutex()

  suspend fun show(data: PromptData): PromptResult = mutex.withLock {
    try {
      suspendCancellableCoroutine { continuation ->
        current = PromptRequest(data, continuation)
      }
    } finally {
      current = null
    }
  }
}

@Composable
fun rememberPromptHostState(): PromptHostState = remember { PromptHostState() }

internal class PromptRequest(
  val data: PromptData,
  private val continuation: CancellableContinuation<PromptResult>,
) {
  fun resolve(result: PromptResult) {
    if (continuation.isActive) continuation.resume(result)
  }
}
