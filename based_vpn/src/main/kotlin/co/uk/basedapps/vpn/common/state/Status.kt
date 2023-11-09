package co.uk.basedapps.vpn.common.state

sealed interface Status {
  object Loading : Status
  object Data : Status
  data class Error(val isLoading: Boolean) : Status
}
