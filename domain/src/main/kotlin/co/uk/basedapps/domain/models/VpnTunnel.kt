package co.uk.basedapps.domain.models

interface VpnTunnel {
  val name: String
  var state: State
  var serverId: String?
  var subscriptionId: Long?

  enum class State {
    DOWN, TOGGLE, UP;

    fun of(running: Boolean): State {
      return if (running) UP else DOWN
    }

    companion object {
      fun of(running: Boolean): State {
        return if (running) UP else DOWN
      }
    }
  }
}
