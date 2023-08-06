package co.uk.basedapps.vpn.common

sealed interface Status {
  object Loading : Status
  object Data : Status
  data class Error(val isLoading: Boolean) : Status
}
