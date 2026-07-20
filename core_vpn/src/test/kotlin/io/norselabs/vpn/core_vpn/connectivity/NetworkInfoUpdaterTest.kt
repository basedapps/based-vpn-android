package io.norselabs.vpn.core_vpn.connectivity

import arrow.core.Either
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.norselabs.vpn.core_vpn.user.UserInitializer
import io.norselabs.vpn.sdk.common.SdkError
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.services.connection.api.NetworkData
import io.norselabs.vpn.sdk.services.connection.api.NetworkInfo
import io.norselabs.vpn.v2ray.model.VpnConnection
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkInfoUpdaterTest {

  private val connectionState = MutableStateFlow<VpnConnection>(VpnConnection.Disconnected)
  private val networkState = MutableSharedFlow<NetworkState>(replay = 1)

  private val dvpn: DVPNClient = mockk()
  private val vpnRepo: V2RayRepository = mockk {
    every { connectionState } returns this@NetworkInfoUpdaterTest.connectionState
  }
  private val networkMonitor: NetworkStateMonitor = mockk {
    every { networkState } returns this@NetworkInfoUpdaterTest.networkState
  }
  private val userInitializer: UserInitializer = mockk {
    coEvery { waitForDeviceToken() } returns Unit
  }

  private var getIpDataCalls = 0

  @After
  fun tearDown() {
    unmockkAll()
  }

  // null answer = SdkError, string = successful response with that ip
  private fun answerIpData(vararg answers: String?) {
    coEvery { dvpn.getIpData() } answers {
      val answer = answers[minOf(getIpDataCalls, answers.lastIndex)]
      getIpDataCalls++
      when (answer) {
        null -> Either.Left(SdkError.HttpError(500, "err", "reason"))
        else -> Either.Right(networkData(answer))
      }
    }
  }

  @Test
  fun `network event while tunnel is establishing does not fetch`() = runTest {
    answerIpData("1.1.1.1")
    NetworkInfoUpdater(backgroundScope, dvpn, vpnRepo, networkMonitor, userInitializer)

    connectionState.value = VpnConnection.CoreStarted
    networkState.emit(NetworkState.Connected(isVpn = true))
    advanceTimeBy(30_000)

    assertEquals(0, getIpDataCalls)
  }

  @Test
  fun `network event without tunnel fetches`() = runTest {
    answerIpData("1.1.1.1")
    val updater = NetworkInfoUpdater(backgroundScope, dvpn, vpnRepo, networkMonitor, userInitializer)

    networkState.emit(NetworkState.Connected(isVpn = false))
    runCurrent()

    assertEquals("1.1.1.1", updater.networkData.value?.ip)
  }

  @Test
  fun `transition to Connected fetches`() = runTest {
    answerIpData("2.2.2.2")
    val updater = NetworkInfoUpdater(backgroundScope, dvpn, vpnRepo, networkMonitor, userInitializer)

    connectionState.value = VpnConnection.Connecting
    networkState.emit(NetworkState.Connected(isVpn = true))
    connectionState.value = VpnConnection.CoreStarted
    runCurrent()
    assertNull(updater.networkData.value)
    assertEquals(0, getIpDataCalls)

    connectionState.value = VpnConnection.Connected(server = "srv", protocol = "Vmess")
    runCurrent()

    assertEquals("2.2.2.2", updater.networkData.value?.ip)
  }

  @Test
  fun `unchanged ip is accepted on the first attempt`() = runTest {
    answerIpData("1.1.1.1")
    val updater = NetworkInfoUpdater(backgroundScope, dvpn, vpnRepo, networkMonitor, userInitializer)
    networkState.emit(NetworkState.Connected(isVpn = false))
    runCurrent()
    assertEquals("1.1.1.1", updater.networkData.value?.ip)

    // A redundant trigger (e.g. the network callback after a disconnect) must
    // not spin retries just because the IP didn't change.
    getIpDataCalls = 0
    networkState.emit(NetworkState.Connected(isVpn = false))
    advanceTimeBy(30_000)

    assertEquals(1, getIpDataCalls)
    assertEquals("1.1.1.1", updater.networkData.value?.ip)
  }

  @Test
  fun `failed fetch is retried up to MAX_ATTEMPTS`() = runTest {
    answerIpData(null, null, "3.3.3.3")
    val updater = NetworkInfoUpdater(backgroundScope, dvpn, vpnRepo, networkMonitor, userInitializer)
    networkState.emit(NetworkState.Connected(isVpn = false))
    runCurrent()
    assertNull(updater.networkData.value)

    advanceTimeBy(30_000)
    assertEquals("3.3.3.3", updater.networkData.value?.ip)
    assertEquals(3, getIpDataCalls)
  }

  @Test
  fun `gives up after MAX_ATTEMPTS failures`() = runTest {
    answerIpData(null)
    val updater = NetworkInfoUpdater(backgroundScope, dvpn, vpnRepo, networkMonitor, userInitializer)
    networkState.emit(NetworkState.Connected(isVpn = false))
    advanceTimeBy(30_000)

    assertNull(updater.networkData.value)
    assertEquals(NetworkInfoUpdater.MAX_ATTEMPTS, getIpDataCalls)
  }

  @Test
  fun `new trigger cancels a pending retry loop`() = runTest {
    // Failing answers keep the first loop retrying...
    answerIpData(null)
    val updater = NetworkInfoUpdater(backgroundScope, dvpn, vpnRepo, networkMonitor, userInitializer)
    networkState.emit(NetworkState.Connected(isVpn = false))
    runCurrent()
    advanceTimeBy(1_500)
    assertEquals(2, getIpDataCalls)

    // ...then a connect retriggers: the old loop dies, one new loop succeeds.
    answerIpData("4.4.4.4")
    connectionState.value = VpnConnection.Connected(server = "srv", protocol = "Vmess")
    runCurrent()
    advanceTimeBy(30_000)

    assertEquals("4.4.4.4", updater.networkData.value?.ip)
    assertEquals(3, getIpDataCalls)
  }

  private fun networkData(ip: String) = NetworkData(
    ip = ip,
    info = NetworkInfo(
      lat = 1.0,
      long = 2.0,
      countryCode = "AR",
      country = "Argentina",
      city = "Olivos",
      provider = "ISP",
    ),
  )
}
