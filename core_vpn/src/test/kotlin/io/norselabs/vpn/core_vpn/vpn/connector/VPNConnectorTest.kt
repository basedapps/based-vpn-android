package io.norselabs.vpn.core_vpn.vpn.connector

import android.util.Base64
import arrow.core.Either
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.Protocol
import io.norselabs.vpn.sdk.common.SdkError
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.services.connection.api.CredentialsResponse
import io.norselabs.vpn.sdk.services.connection.api.ServerShort
import io.norselabs.vpn.v2ray.error.V2RayError
import io.norselabs.vpn.v2ray.model.VpnProfile
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VPNConnectorTest {

  private val clock = TestClock(start = 1_000L)
  private val interactor = RecordingInteractor()
  private lateinit var gson: Gson
  private lateinit var dvpn: DVPNClient
  private lateinit var coreStorage: CoreStorage
  private lateinit var connector: VPNConnector

  @Before
  fun setUp() {
    mockkStatic(Base64::class)
    every { Base64.decode(any<String>(), any<Int>()) } returns ByteArray(64) { it.toByte() }
    every { Base64.encode(any<ByteArray>(), any<Int>()) } returns ByteArray(32) { it.toByte() }

    gson = Gson()
    dvpn = mockk(relaxed = true)
    coreStorage = mockk(relaxed = true)
    every { coreStorage.getVpnProtocol() } returns Protocol.WIREGUARD
    every { coreStorage.setCurrentServerId(any()) } just runs

    connector = VPNConnector(
      gson = gson,
      dvpn = dvpn,
      coreStorage = coreStorage,
      driver = interactor,
      listener = interactor,
      clock = clock::now,
    )
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `successful connect followed by user disconnect produces started, received, disconnect`() = runTest {
    val destination = country()
    coEvery { dvpn.getCountryCredentials("ru", "WIREGUARD") } answers {
      clock.advance(50)
      Either.Right(wireguardResponse("srv1"))
    }
    interactor.startVpnResult = Either.Right(Unit)

    val result = connector.connect(destination)
    assertTrue(result.isRight())

    clock.advance(2_000)
    connector.disconnect(DisconnectReason.UserRequested)

    assertEquals(3, interactor.events.size)
    val started = interactor.events[0] as ConnectStartedEvent
    val received = interactor.events[1] as CredentialsReceivedEvent
    val disconnected = interactor.events[2] as DisconnectEvent

    assertEquals(started.attemptId, received.attemptId)
    assertEquals(started.attemptId, disconnected.attemptId)
    assertEquals(destination, started.destination)
    assertEquals(Protocol.WIREGUARD, started.protocol)
    assertEquals("srv1", received.serverId)
    assertEquals(50L, received.durationMs)
    assertEquals(DisconnectReason.UserRequested, disconnected.reason)
    assertEquals(2_000L, disconnected.sessionDurationMs)
    assertEquals(started.attemptId, interactor.lastStartAttemptId)
    assertEquals(DisconnectReason.UserRequested, interactor.lastStopReason)
  }

  @Test
  fun `credentials fetch failure produces started, failed and no disconnect`() = runTest {
    val destination = country()
    coEvery { dvpn.getCountryCredentials("ru", "WIREGUARD") } answers {
      clock.advance(100)
      Either.Left(SdkError.HttpError(500, "err", "reason"))
    }

    val result = connector.connect(destination)
    assertTrue(result.isLeft())

    assertEquals(2, interactor.events.size)
    val started = interactor.events[0] as ConnectStartedEvent
    val failed = interactor.events[1] as CredentialsFailedEvent

    assertEquals(started.attemptId, failed.attemptId)
    assertEquals(100L, failed.durationMs)
    assertEquals(
      CredentialsError.ServerError(httpCode = 500, errorCode = "err", message = "reason"),
      failed.error,
    )
  }

  @Test
  fun `tunnel failure after up produces disconnect with TunnelError reason`() = runTest {
    val destination = country()
    coEvery { dvpn.getCountryCredentials("ru", "WIREGUARD") } answers {
      clock.advance(40)
      Either.Right(wireguardResponse("srv1"))
    }
    interactor.startVpnResult = Either.Right(Unit)

    val result = connector.connect(destination)
    assertTrue(result.isRight())

    clock.advance(3_000)
    connector.disconnect(DisconnectReason.TunnelEstablishFailed(V2RayError.StartV2Ray))

    assertEquals(3, interactor.events.size)
    val started = interactor.events[0] as ConnectStartedEvent
    val received = interactor.events[1] as CredentialsReceivedEvent
    val disconnected = interactor.events[2] as DisconnectEvent

    assertEquals(started.attemptId, received.attemptId)
    assertEquals(started.attemptId, disconnected.attemptId)
    assertEquals(DisconnectReason.TunnelEstablishFailed(V2RayError.StartV2Ray), disconnected.reason)
    assertEquals(3_000L, disconnected.sessionDurationMs)
  }

  private fun country() = Destination.Country(
    countryId = "ru",
    countryName = "Russia",
    countryCode = "RU",
  )

  private fun wireguardResponse(serverId: String) = CredentialsResponse(
    protocol = Protocol.WIREGUARD.strValue,
    payload = "payload",
    privateKey = "private-key",
    uid = null,
    server = ServerShort(id = serverId),
  )

  private class TestClock(start: Long) {
    @Volatile
    private var current = start

    fun now(): Long = current

    fun advance(ms: Long) {
      current += ms
    }
  }

  private class RecordingInteractor : VPNDriver, ConnectionLifecycleListener {
    var startVpnResult: Either<V2RayError, Unit> = Either.Right(Unit)
    var lastStartAttemptId: AttemptId? = null
    var lastStopReason: DisconnectReason? = null
    private var connected = false
    val events = mutableListOf<Any>()

    override suspend fun startVpn(
      vpnProfile: VpnProfile,
      attemptId: AttemptId,
    ): Either<V2RayError, Unit> {
      lastStartAttemptId = attemptId
      val r = startVpnResult
      if (r.isRight()) connected = true
      return r
    }

    override fun isVpnConnected(): Boolean = connected
    override fun stopVpn(reason: DisconnectReason) {
      lastStopReason = reason
      connected = false
    }

    override fun resetNetworkClient() = Unit

    override fun onConnectStarted(event: ConnectStartedEvent) {
      events += event
    }

    override fun onCredentialsReceived(event: CredentialsReceivedEvent) {
      events += event
    }

    override fun onCredentialsFailed(event: CredentialsFailedEvent) {
      events += event
    }

    override fun onDisconnect(event: DisconnectEvent) {
      events += event
    }
  }
}
