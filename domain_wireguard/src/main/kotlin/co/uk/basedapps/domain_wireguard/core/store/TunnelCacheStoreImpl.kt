package co.uk.basedapps.domain_wireguard.core.store

import co.uk.basedapps.domain.functional.SortedKeyedArrayList
import co.uk.basedapps.domain_wireguard.core.model.TunnelComparator
import co.uk.basedapps.domain_wireguard.core.model.TunnelWrapper

class TunnelCacheStoreImpl : TunnelCacheStore {
  private val tunnelMap = SortedKeyedArrayList<String, TunnelWrapper>(TunnelComparator)
  private var lastUsedTunnel: TunnelWrapper? = null

  override fun add(tunnel: TunnelWrapper) {
    if (!tunnelMap.containsKey(tunnel.key)) {
      tunnelMap.add(tunnel)
    }
  }

  override fun populate(tunnelList: List<TunnelWrapper>) {
    tunnelMap.clear()
    tunnelMap.addAll(tunnelList)
  }

  override fun delete(tunnel: TunnelWrapper) {
    tunnelMap.remove(tunnel)
  }

  override fun getTunnelList() = tunnelMap

  override fun updateLastUsedTunnel(tunnel: TunnelWrapper?) {
    lastUsedTunnel = tunnel
  }

  override fun getLastUsedTunnel() = lastUsedTunnel
}
