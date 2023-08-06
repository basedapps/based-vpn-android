package co.uk.basedapps.domain_wireguard.core.repo

import android.annotation.SuppressLint
import android.content.Context
import co.uk.basedapps.domain.functional.Either
import co.uk.basedapps.domain.models.KeyPair as DomainKeyPair
import co.uk.basedapps.domain.models.VpnTunnel
import co.uk.basedapps.domain_wireguard.core.init.DefaultTunnelName
import co.uk.basedapps.domain_wireguard.core.mapper.ConfigToDomainConfigMapper
import co.uk.basedapps.domain_wireguard.core.mapper.DomainConfigToConfigMapper
import co.uk.basedapps.domain_wireguard.core.mapper.DomainStateToStateMapper
import co.uk.basedapps.domain_wireguard.core.mapper.DomainTunnelToTunnelMapper
import co.uk.basedapps.domain_wireguard.core.mapper.TunnelToDomainTunnelMapper
import co.uk.basedapps.domain_wireguard.core.model.TunnelConfig
import co.uk.basedapps.domain_wireguard.core.model.TunnelInterface
import co.uk.basedapps.domain_wireguard.core.model.TunnelPeer
import co.uk.basedapps.domain_wireguard.core.model.TunnelWrapper
import co.uk.basedapps.domain_wireguard.core.model.WireguardTunnel
import co.uk.basedapps.domain_wireguard.core.model.WireguardVpnProfile
import co.uk.basedapps.domain_wireguard.core.store.ConfigStore
import co.uk.basedapps.domain_wireguard.core.store.TunnelCacheStore
import co.uk.basedapps.domain_wireguard.core.store.WireguardUserPreferenceStore
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.WgQuickBackend
import com.wireguard.android.util.RootShell
import com.wireguard.android.util.ToolsInstaller
import com.wireguard.config.Config
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

class WireguardRepositoryImpl(
  private val context: Context,
  private val configStore: ConfigStore,
  private val tunnelCacheStore: TunnelCacheStore,
  private val userPreferenceStore: WireguardUserPreferenceStore,
) : WireguardRepository {
  companion object {
    const val DEFAULT_DNS = "103.196.38.38, 103.196.38.39, 103.196.38.40"
  }

  private var backend: Backend? = null
  private lateinit var rootShell: RootShell
  private lateinit var toolsInstaller: ToolsInstaller

  private var haveLoaded = false

  override suspend fun init(alwaysOnCallback: () -> Unit) {
    rootShell = RootShell(context)
    toolsInstaller = ToolsInstaller(context, rootShell)
    backend = determineBackend(alwaysOnCallback)
  }

  private fun determineBackend(alwaysOnCallback: () -> Unit): Backend {
    var backend: Backend? = null
    try {
      rootShell.start()
      val wgQuickBackend = WgQuickBackend(context, rootShell, toolsInstaller)
      wgQuickBackend.setMultipleTunnels(false)
      backend = wgQuickBackend
    } catch (ignored: Exception) {
    }

    if (backend == null) {
      backend = GoBackend(context)
      GoBackend.setAlwaysOnCallback { alwaysOnCallback() }
    }
    return backend
  }

  private fun addTunnelToList(
    name: String,
    state: Tunnel.State,
    config: Config?,
  ) =
    TunnelWrapper(name, state, config).also {
      tunnelCacheStore.add(it)
    }

  override suspend fun getTunnels(): List<WireguardTunnel>? {
    return try {
      val tunnels = tunnelCacheStore.getTunnelList()
      if (tunnels.isEmpty()) {
        val isLoadSuccess = loadTunnels()
        if (isLoadSuccess) {
          tunnelCacheStore.getTunnelList()
            .map { TunnelToDomainTunnelMapper.map(it) }
        } else {
          null
        }
      } else {
        tunnels.map { TunnelToDomainTunnelMapper.map(it) }
      }
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }

  override suspend fun getTunnel(tunnelName: String): VpnTunnel? {
    return try {
      getTunnels()
        ?.firstOrNull { it.name == tunnelName }
        ?.apply { serverId = userPreferenceStore.serverId.firstOrNull() }
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }

  @SuppressLint("CheckResult")
  override suspend fun createOrUpdate(
    name: String,
    vpnProfile: WireguardVpnProfile,
    keyPair: DomainKeyPair,
    serverId: String,
  ): Either<Exception?, WireguardTunnel> =
    kotlin.runCatching {
      vpnProfile.let {
        if (Tunnel.isNameInvalid(name)) {
          Either.Left(Exception("InvalidName"))
        } else {
          if (tunnelCacheStore.getTunnelList().containsKey(name)) {
            delete(name)
          }
          addTunnelToList(
            name = name,
            state = Tunnel.State.DOWN,
            config = configStore.create(
              name,
              DomainConfigToConfigMapper.map(
                TunnelConfig(
                  tunnelInterface = TunnelInterface(
                    excludedApplications = listOf(),
                    includedApplications = listOf(),
                    addresses = vpnProfile.address,
                    dnsServers = userPreferenceStore.dns.firstOrNull()
                      ?: DEFAULT_DNS,
                    listenPort = vpnProfile.listenPort,
                    mtu = "800",
                    privateKey = keyPair.privateKey,
                    publicKey = keyPair.publicKey,
                  ),
                  peers = listOf(
                    TunnelPeer(
                      allowedIps = "0.0.0.0/0",
                      endpoint = vpnProfile.peerEndpoint,
                      persistentKeepAlive = "25",
                      preSharedKey = null,
                      publicKey = Key.fromBytes(vpnProfile.peerPubKeyBytes)
                        .toBase64(),
                    ),
                  ),
                ),
              ),
            ),
          ).let {
            userPreferenceStore.setServerId(serverId)
            Either.Right(TunnelToDomainTunnelMapper.map(it))
          }
        }
      }
    }
      .onFailure { Timber.e(it) }
      .getOrNull() ?: Either.Left(null)

  override suspend fun create(
    name: String,
    tunnelConfig: TunnelConfig,
  ): Either<Exception?, WireguardTunnel> =
    kotlin.runCatching {
      when {
        Tunnel.isNameInvalid(name) -> Either.Left(Exception("InvalidName"))
        tunnelCacheStore.getTunnelList().containsKey(name) -> Either.Left(Exception("NameAlreadyExists"))
        else -> addTunnelToList(
          name = name,
          state = Tunnel.State.DOWN,
          config = configStore.create(
            name,
            DomainConfigToConfigMapper.map(tunnelConfig),
          ),
        ).let {
          Either.Right(TunnelToDomainTunnelMapper.map(it))
        }
      }
    }.onFailure {
      Timber.e(it)
    }.getOrNull() ?: Either.Left(null)

  override suspend fun delete(name: String): Either<Unit, Unit> =
    kotlin.runCatching {
      tunnelCacheStore.getTunnelList().let {
        it[name] ?: throw IllegalStateException()
      }.let { tunnel ->
        val originalState = tunnel.state
        val wasLastUsed = tunnel == tunnelCacheStore.getLastUsedTunnel()
        // Make sure nothing touches the tunnel.
        if (wasLastUsed) {
          tunnelCacheStore.updateLastUsedTunnel(null)
        }
        tunnelCacheStore.delete(tunnel)
        try {
          if (originalState == Tunnel.State.UP) {
            backend?.setState(tunnel, Tunnel.State.DOWN, null)
          }
          try {
            configStore.delete(tunnel.name)
          } catch (e: Throwable) {
            if (originalState == Tunnel.State.UP) {
              backend?.setState(tunnel, Tunnel.State.UP, tunnel.config)
            }
            throw e
          }
        } catch (e: Throwable) {
          // Failure, put the tunnel back.
          tunnelCacheStore.add(tunnel)
          if (wasLastUsed) {
            tunnelCacheStore.updateLastUsedTunnel(tunnel)
          }
          throw e
        }
        Either.Right(Unit)
      }
    }.onFailure {
      Timber.e(it)
    }.getOrNull() ?: Either.Left(Unit)

  override suspend fun isConnected(): Boolean =
    getTunnel(DefaultTunnelName)
      ?.let { it.state == VpnTunnel.State.UP }
      ?: false

  override suspend fun getTunnelConfig(tunnelName: String): Either<Exception?, TunnelConfig> =
    kotlin.runCatching {
      tunnelCacheStore.getTunnelList().firstOrNull { it.name == tunnelName }
        ?.let { tunnel ->
          with(configStore.load(tunnel.name)) {
            // sync up tunnel config in the store.
            tunnel.config = this
            Either.Right(ConfigToDomainConfigMapper.map(this))
          }
        } ?: Either.Left(Exception("TunnelNotFound"))
    }.onFailure {
      Timber.e(it)
    }.getOrNull() ?: Either.Left(null)

  override suspend fun loadTunnels(): Boolean {
    return try {
      val lastUsedTunnel = userPreferenceStore.lastUsedTunnel.firstOrNull()
      val present = configStore.enumerate()
      val running = backend?.runningTunnelNames
      for (name in present)
        addTunnelToList(
          name = name,
          state = if (running?.contains(name) == true) Tunnel.State.UP else Tunnel.State.DOWN,
          config = configStore.load(name),
        )
      if (lastUsedTunnel != null) {
        tunnelCacheStore.updateLastUsedTunnel(
          tunnelCacheStore.getTunnelList()[lastUsedTunnel],
        )
      }
      haveLoaded = true
      restoreState(isForce = true)
      true
    } catch (e: Exception) {
      Timber.e(e)
      false
    }
  }

  override fun refreshTunnelStates() {
    // todo refactor when needed maybe?
    try {
      val running = backend?.runningTunnelNames ?: return
      for (tunnel in tunnelCacheStore.getTunnelList())
        tunnel.onStateChanged(if (running.contains(tunnel.name)) Tunnel.State.UP else Tunnel.State.DOWN)
    } catch (e: Throwable) {
      Timber.e(e)
    }
  }

  override suspend fun restoreState(isForce: Boolean) {
    val restoreOnBoot = userPreferenceStore.restoreOnBoot.firstOrNull() ?: false
    val previouslyRunning = userPreferenceStore.runningTunnels.firstOrNull() ?: setOf()
    if (!haveLoaded || (!isForce && !restoreOnBoot)) {
      return
    }
    if (previouslyRunning.isEmpty()) {
      return
    }
    try {
      tunnelCacheStore.getTunnelList()
        .filter { previouslyRunning.contains(it.name) }
        .map {
          setTunnelState(it, Tunnel.State.UP)
        }
    } catch (e: Throwable) {
      Timber.e(e)
    }
  }

  override suspend fun saveState() {
    userPreferenceStore.setRunningTunnels(
      tunnelCacheStore.getTunnelList()
        .filter { it.state == Tunnel.State.UP }
        .map { it.name }
        .toSet(),
    )
  }

  override suspend fun setTunnelConfig(
    tunnelName: String,
    tunnelConfig: TunnelConfig,
  ): Either<Unit, Unit> {
    return kotlin.runCatching {
      val config = DomainConfigToConfigMapper.map(tunnelConfig)
      return backend?.let {
        val tunnel = tunnelCacheStore.getTunnelList()[tunnelName]
        if (tunnel == null) {
          Either.Left(Unit)
        } else {
          it.setState(tunnel, tunnel.state, config)
          configStore.save(tunnel.name, config)
          Either.Right(Unit)
        }
      } ?: Either.Left(Unit)
    }.onFailure { Timber.e(it) }
      .getOrNull() ?: Either.Left(Unit)
  }

  override suspend fun updateDns(dns: String): Either<Unit, Unit> {
    return kotlin.runCatching {
      userPreferenceStore.setDns(dns)
      val tunnel = tunnelCacheStore.getTunnelList()
        .firstOrNull { it.state == Tunnel.State.UP }
      return tunnel?.config?.let { config ->
        ConfigToDomainConfigMapper.map(config).let { domainConfig ->
          setTunnelConfig(
            tunnelName = tunnel.name,
            tunnelConfig = domainConfig.copy(
              tunnelInterface = domainConfig.tunnelInterface.copy(
                dnsServers = dns,
              ),
            ),
          ).let {
            if (it.isRight) {
              Either.Right(Unit)
            } else {
              Either.Left(Unit)
            }
          }
        }
      } ?: Either.Right(Unit) // if there is no tunnel, it means we haven't ever connected
    }.onFailure { Timber.e(it) }
      .getOrNull() ?: Either.Left(Unit)
  }

  override suspend fun getDefaultDns() =
    userPreferenceStore.dns.firstOrNull() ?: DEFAULT_DNS

  override suspend fun setTunnelName(
    newTunnelName: String,
    wgTunnel: WireguardTunnel,
  ): Either<Exception?, Unit> {
    return kotlin.runCatching {
      val tunnel = DomainTunnelToTunnelMapper.map(wgTunnel)
      when {
        Tunnel.isNameInvalid(newTunnelName) -> Either.Left(Exception("InvalidName"))
        tunnelCacheStore.getTunnelList().containsKey(newTunnelName) -> Either.Left(Exception("NameAlreadyExists"))
        else -> {
          val originalState = tunnel.state
          val wasLastUsed = tunnel == tunnelCacheStore.getLastUsedTunnel()
          // Make sure nothing touches the tunnel.
          if (wasLastUsed) {
            tunnelCacheStore.updateLastUsedTunnel(null)
          }
          tunnelCacheStore.delete(tunnel)
          var throwable: Throwable? = null
          try {
            if (originalState == Tunnel.State.UP) {
              backend?.setState(tunnel, Tunnel.State.DOWN, null)
                ?: throw IllegalStateException()
            }
            configStore.rename(tunnel.name, newTunnelName)
            tunnel.onNameChanged(newTunnelName)
            if (originalState == Tunnel.State.UP) {
              backend?.setState(tunnel, Tunnel.State.UP, tunnel.config)
                ?: throw IllegalStateException()
            }
          } catch (e: Throwable) {
            throwable = e
            // On failure, we don't know what state the tunnel might be in. Fix that.
            getTunnelState(tunnel)
          }
          // Add the tunnel back to the manager, under whatever name it thinks it has.
          tunnelCacheStore.add(tunnel)
          if (wasLastUsed) {
            tunnelCacheStore.updateLastUsedTunnel(tunnel)
          }
          if (throwable != null) {
            return Either.Left(null)
          }

          Either.Right(Unit)
        }
      }
    }.onFailure { Timber.e(it) }
      .getOrNull() ?: Either.Left(null)
  }

  override suspend fun setTunnelState(
    tunnelName: String,
    tunnelState: VpnTunnel.State,
  ): Either<Unit, WireguardTunnel> =
    kotlin.runCatching {
      tunnelCacheStore.getTunnelList()[tunnelName]?.let { tunnel ->
        setTunnelState(tunnel, DomainStateToStateMapper.map(tunnelState))
      }
    }.onFailure { Timber.e(it) }
      .getOrNull() ?: Either.Left(Unit)

  private suspend fun setTunnelState(
    tunnel: TunnelWrapper,
    state: Tunnel.State,
  ): Either<Unit, WireguardTunnel> = kotlin.runCatching {
    configStore.load(tunnel.name).let {
      var newState = state
      var throwable: Throwable? = null
      try {
        backend?.let {
          newState = it.setState(tunnel, state, tunnel.config)
        } ?: throw IllegalStateException()

        if (newState == Tunnel.State.UP) {
          tunnelCacheStore.updateLastUsedTunnel(tunnel)
        }
      } catch (e: Throwable) {
        throwable = e
      }

      tunnel.onStateChanged(newState)
      saveState()

      if (throwable != null) {
        Either.Left(Unit)
      } else {
        Either.Right(TunnelToDomainTunnelMapper.map(tunnel))
      }
    }
  }.onFailure { Timber.e(it) }
    .getOrNull() ?: Either.Left(Unit)

  private fun getTunnelState(tunnel: TunnelWrapper) {
    backend?.let {
      tunnel.onStateChanged(it.getState(tunnel))
    }
  }

  override suspend fun getTunnelStatistics(tunnelName: String): Either<Exception?, Unit> =
    kotlin.runCatching {
      val tunnelName = tunnelName
      val tunnel = tunnelCacheStore.getTunnelList()[tunnelName]
      if (tunnel == null) {
        Either.Left(Exception("TunnelNotFound"))
      } else {
        tunnel.onStatisticsChanged(
          backend?.getStatistics(tunnel) ?: return Either.Left(null),
        )
        Either.Right(Unit)
      }
    }.onFailure { Timber.e(it) }
      .getOrNull() ?: Either.Left(null)

  override fun generateKeyPair() = with(KeyPair()) {
    DomainKeyPair(
      privateKey = privateKey.toBase64(),
      publicKey = publicKey.toBase64(),
    )
  }

  override fun generateKeyPair(privateKey: String): DomainKeyPair {
    return with(KeyPair(Key.fromBase64(privateKey))) {
      DomainKeyPair(
        privateKey = privateKey,
        publicKey = publicKey.toBase64(),
      )
    }
  }
}
