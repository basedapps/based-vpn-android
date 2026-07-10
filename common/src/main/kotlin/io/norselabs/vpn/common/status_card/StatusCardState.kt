package io.norselabs.vpn.common.status_card

sealed interface StatusCardState {
  val title: String
  val subtitle: String?

  data class Loading(
    override val title: String,
    override val subtitle: String? = null,
  ) : StatusCardState

  data class Success(
    override val title: String,
    override val subtitle: String? = null,
  ) : StatusCardState

  data class Info(
    override val title: String,
    override val subtitle: String? = null,
  ) : StatusCardState

  data class Error(
    override val title: String,
    override val subtitle: String? = null,
    val retryable: Boolean = false,
  ) : StatusCardState
}
