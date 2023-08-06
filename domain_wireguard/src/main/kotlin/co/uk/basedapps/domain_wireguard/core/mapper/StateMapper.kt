package co.uk.basedapps.domain_wireguard.core.mapper

import co.uk.basedapps.domain.functional.Mapper
import co.uk.basedapps.domain.models.VpnTunnel.State as DomainState
import com.wireguard.android.backend.Tunnel

object StateToDomainStateMapper : Mapper<Tunnel.State, DomainState> {
  override fun map(obj: Tunnel.State) = when (obj) {
    Tunnel.State.DOWN -> DomainState.DOWN
    Tunnel.State.TOGGLE -> DomainState.TOGGLE
    Tunnel.State.UP -> DomainState.UP
  }
}

object DomainStateToStateMapper : Mapper<DomainState, Tunnel.State> {
  override fun map(obj: DomainState) = when (obj) {
    DomainState.DOWN -> Tunnel.State.DOWN
    DomainState.TOGGLE -> Tunnel.State.TOGGLE
    DomainState.UP -> Tunnel.State.UP
  }
}
