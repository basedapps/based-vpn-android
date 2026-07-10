package co.uk.basedapps.vpn.ui.screens.demo

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.norselabs.vpn.common.status_card.StatusCardController
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StatusCardDemoViewModel
@Inject constructor(
  val statusCard: StatusCardController,
) : ScreenModel {

  private var scenarioJob: Job? = null

  init {
    statusCard.retryClicks
      .onEach { onScenarioClick() }
      .launchIn(screenModelScope)
  }

  fun onScenarioClick() {
    scenarioJob?.cancel()
    scenarioJob = screenModelScope.launch {
      for (attempt in 1..5) {
        statusCard.showLoading(title = "Connecting… ($attempt/5)")
        delay(600)
      }
      statusCard.showLoading(title = "Preparing your account…")
      delay(1200)
      statusCard.showSuccess(title = "Account prepared")
      delay(1500)
      statusCard.showError(
        title = "Something went wrong",
        subtitle = "Tap retry to restart the scenario.",
        retryable = true,
      )
    }
  }

  fun onLoadingClick() {
    scenarioJob?.cancel()
    statusCard.showLoading(title = "Connecting… (9/28)")
  }

  fun onSuccessClick() {
    scenarioJob?.cancel()
    statusCard.showSuccess(
      title = "Account prepared",
      subtitle = "Hides automatically in 2s.",
      hideAfter = 2.seconds,
    )
  }

  fun onInfoClick() {
    scenarioJob?.cancel()
    statusCard.showInfo(
      title = "New servers available",
      subtitle = "Check the updated locations list.",
    )
  }

  fun onErrorClick() {
    scenarioJob?.cancel()
    statusCard.showError(
      title = "Something went wrong",
      subtitle = "Check your connection.",
      retryable = true,
    )
  }

  fun onHideClick() {
    scenarioJob?.cancel()
    statusCard.hide()
  }

  override fun onDispose() {
    statusCard.hide()
  }
}
