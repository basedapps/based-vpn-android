package co.sentinel.vpn.based.vpn

import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplitTunnelingConfigurator @Inject constructor(
  private val repo: V2RayRepository,
) {

  fun getStatus(): SplitTunnelingStatus {
    return when {
      !repo.isPerAppProxyEnabled() -> SplitTunnelingStatus.Disabled
      repo.isBypassEnabled() -> SplitTunnelingStatus.Bypass
      else -> SplitTunnelingStatus.Enabled
    }
  }

  fun setStatus(status: SplitTunnelingStatus) {
    repo.enablePerAppProxy(status != SplitTunnelingStatus.Disabled)
    repo.enableBypass(status == SplitTunnelingStatus.Bypass)
  }

  fun getApps(): Set<String> {
    return repo.getPerAppProxySet()
  }

  fun setApps(apps: Set<String>) {
    repo.setPerAppProxySet(apps)
  }
}

enum class SplitTunnelingStatus {
  Disabled,
  Enabled,
  Bypass,
}
