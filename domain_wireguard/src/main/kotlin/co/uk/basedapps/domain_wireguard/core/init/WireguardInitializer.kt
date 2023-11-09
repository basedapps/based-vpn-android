package co.uk.basedapps.domain_wireguard.core.init

import co.uk.basedapps.domain_wireguard.core.repo.WireguardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WireguardInitializer(
  private val repository: WireguardRepository,
) {

  private val scope = CoroutineScope(Dispatchers.Main)

  fun init() {
    scope.launch(Dispatchers.IO) {
      repository.init(::handleAlwaysOn)
      repository.loadTunnels()
    }
  }

  private fun handleAlwaysOn() {
    scope.launch(Dispatchers.IO) {
      repository.restoreState(isForce = true)
    }
  }
}
