package co.sentinel.vpn.based.viewModel.split_tunneling

interface NetAppsProvider {

  suspend fun getNetApps(): List<NetworkApp>
}
