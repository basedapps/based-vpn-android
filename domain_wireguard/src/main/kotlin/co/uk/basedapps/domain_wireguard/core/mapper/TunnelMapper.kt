package co.uk.basedapps.domain_wireguard.core.mapper

import co.uk.basedapps.domain.functional.Mapper
import co.uk.basedapps.domain_wireguard.core.model.TunnelWrapper
import co.uk.basedapps.domain_wireguard.core.model.WireguardTunnel

object DomainTunnelToTunnelMapper : Mapper<WireguardTunnel, TunnelWrapper> {
  override fun map(obj: WireguardTunnel) = TunnelWrapper(
    name = obj.name,
    state = DomainStateToStateMapper.map(obj.state),
    config = obj.config?.let { DomainConfigToConfigMapper.map(it) },
    duration = obj.duration,
  )
}

object TunnelToDomainTunnelMapper : Mapper<TunnelWrapper, WireguardTunnel> {
  override fun map(obj: TunnelWrapper) = WireguardTunnel(
    name = obj.name,
    state = StateToDomainStateMapper.map(obj.state),
    config = obj.config?.let { ConfigToDomainConfigMapper.map(it) },
    duration = obj.duration,
  )
}
