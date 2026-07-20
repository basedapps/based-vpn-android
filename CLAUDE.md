# CLAUDE.md

Guidance for Claude Code (and other coding agents) working in this repository.
This is the canonical agent doc. For deeper reference see [`docs/`](docs/).

## Project

- **BasedVPN Android** — a monorepo of reusable, independently-published Android
  modules forming the NorseLabs VPN platform's high/mid layers.
- Every library module publishes to the private NorseLabs Nexus under
  `io.norselabs.vpn:*`; the shipping apps (bagmisiz, DVPN) consume them.
- Tech stack: Kotlin, Jetpack Compose, Hilt/KAPT, Voyager navigation, Ktor,
  Arrow, RevenueCat, Branch, Timber, and the sibling Norse VPN libraries
  (`io.norselabs.vpn:v2ray`, `io.norselabs.vpn:dvpn_sdk`).
- Android config: compileSdk 36, minSdk 26, JVM 17.
- **Gradle DSL is Groovy** (`build.gradle`, not `.kts`). Dependency versions live
  in the version catalog [gradle/libs.versions.toml](gradle/libs.versions.toml),
  referenced via `libs.*`.

## Modules & published versions

| Gradle module | Artifact | Version | Role |
|---|---|---|---|
| `:based_vpn` | `based` | 1.6.0 (Nexus) | High-level reuse layer: ViewModels, Hilt DI, `AppConfig` |
| `:core_vpn` | `core_vpn` | 1.3.0 (Nexus) | Low-level VPN orchestration |
| `:common` | `common` | 1.0.0 | Utils, preferences, state holders, StatusCardController |
| `:common_logger` | `common_logger` | 0.0.4 | Timber + file logging + upload |
| `:common_flags` | `common_flags` | 0.0.2 | Country flag assets |
| `:common_map` | `common_map` | 0.0.4 | Compose map components |
| `:common_net_apps` | `common_net_apps` | 0.0.1 | Installed-apps utilities |
| `:common_referral` | `common_referral` | 0.0.4 | Branch + Ads referral |
| `:common_purchases` | `common_purchases` | 0.0.2 | RevenueCat logic |
| `:common_purchases_ui` | `common_purchases_ui` | 0.0.3 | RevenueCat paywall UI |
| `:common_compose` | `common_compose` | 1.2.1 | Reusable Compose components (StatusCard) |
| `:app` | `co.uk.basedapps:vpn` | _not published_ | Original BasedVPN reference app |

Each module's version lives in the `publishing { }` block of its `build.gradle`.
Versions are independent — bump only what changed.

## Commands

Use the Gradle **wrapper**.

```bash
# Build a module / the reference app
./gradlew :based_vpn:assembleDebug
./gradlew :app:assembleDebug

# Publish a library
./gradlew :based_vpn:publishToMavenLocal   # local ~/.m2 (test consumers)
./gradlew :based_vpn:publish               # private Nexus (release)
```

Publish in dependency order — see [docs/integration.md](docs/integration.md).

## Required environment

| Variable | Purpose |
|---|---|
| `NORSELABS_REPO_LOGIN` | Nexus username (read deps + publish) |
| `NORSELABS_REPO_PASSWORD` | Nexus password |

Nexus: `https://nexus.norselabs.dev/repository/maven-releases/`. The reference
`:app` module additionally needs its own app config/secrets to run, like any
wrapper app.

## Architecture at a glance

Two internal layers over the foundation libraries:

- **`core_vpn`** — the engine room: `VPNConnector`, `VPNDriver`, `DnsConfigurator`,
  `SplitTunnelingConfigurator`, `UserInitializer`, `NetworkStateMonitor`,
  `DestinationStorage`, `CoreStorage`. Depends on `v2ray` + `dvpn_sdk` + `common`.
- **`based`** (module `:based_vpn`) — the high-level layer apps build on. Contains
  Voyager `ScreenModel` ViewModels (`viewModel/{dashboard,settings,countries,`
  `cities,servers,split_tunneling,fragment}`), Hilt DI modules (`di/`), the
  `AppConfig` contract (`app_config/`), `VPNDriverImpl` (`core_impl/vpn/` —
  the bridge between `dvpn_sdk` credentials and the `v2ray` tunnel), storage,
  language, and network helpers. `api`-exposes `v2ray`, `dvpn_sdk`, `core_vpn`,
  `common`, `common_logger`, `common_purchases`, `common_flags`.

The extension contract: a wrapper app implements `AppConfig` and provides it as a
Hilt singleton; `based` supplies the rest of the graph and the ViewModels. Full
detail in [docs/architecture.md](docs/architecture.md).

## Conventions

- Code style: **ktlint** with the `android_studio` style, enforced via
  [.editorconfig](.editorconfig). Match surrounding code.
- DI: Hilt with constructor injection; `@Provides`/`@Binds` in `di/` modules
  installed in `SingletonComponent`.
- UI/state: Voyager `ScreenModel` ViewModels; Compose.
- Networking/results: Ktor + Arrow `Either`; `kotlinx.serialization`.
- New libraries go in the version catalog, referenced via `libs.*`.

More in [docs/conventions.md](docs/conventions.md).

## Gotchas

- **`based` re-exports foundation libs with `api`.** Bumping `v2ray` / `dvpn_sdk`
  / `core_vpn` ripples up through `core_vpn` → `based` and is exposed transitively
  to consumers — republish `core_vpn` and `based` after such a bump.
- **`dvpn_sdk` 2.3.0 adopted (Nexus): `getIpData()` tunnel routing.** `DvpnModule.provideDVPN`
  passes a `V2RayTunnelStateProvider` (`core_impl/vpn/`, wraps `V2RayRepository.isConnected()` +
  `getSocksProxyPort()`) into the `DVPNClient(...)` factory, so `getIpData()` egresses through the
  tunnel's local SOCKS5 proxy while connected. Requires `defaultUrl` (already passed from
  `AppConfig.getBaseUrl()`). The never-consumed `AppConfig.getProxy()` hook is removed (breaking
  vs `based` 1.4.2) — the SOCKS port comes from `V2RayRepository.getSocksProxyPort()`, not config.
  See `SolarLabs/docs/tunnel-routing-policy.md`.
- **`v2ray` 2.1.0 adopted.** The catalog pins v2ray 2.1.0 and core_vpn 1.2.3;
  v2ray 2.1.0, core_vpn 1.2.3 and based 1.5.0 are all on **Nexus** (2026-07-14).
  Migration done: `VPNDriver`/`Analytics`/`VPNDriverImpl` use
  `V2RayStartError` (the 1.x `V2RayError` is gone), `ProfileDecoder` maps the
  `VmessTransport` enum (tcp/ws/grpc; unsupported — incl. h2, removed from Xray-core in favor
  of XHTTP — → `null`), dashboard VM
  observes `connectionState` instead of the removed `isConnected` flow. Publish
  `core_vpn` + `based` 1.5.0 to Nexus before releasing consumers.
- **Connect gated on readiness (`based` 1.5.1, Nexus).** `DashboardScreenState.isReadyToConnect`
  (= `cardState == null || cardState is ConnectionCardState.Success`) gates
  `DashboardScreenViewModel.initConnection()`: no connection can start while a busy status card is up
  (mirror discovery / enrollment / error). The transient Success card is exempt (Enrolled + Connected
  already, so connecting stays available). Additive vs 1.5.0. On Nexus since 2026-07-18;
  `bagmisiz` pins it.
- **IP/location refresh lives in `NetworkInfoUpdater` (`core_vpn` 1.3.0 / `based` 1.6.0, Nexus).**
  `core_vpn`'s `connectivity/NetworkInfoUpdater` (UserInitializer-style singleton, provided in
  `based`'s `AppModule` with its own IO scope) owns *when* `getIpData()` runs: it skips OS network
  events while the tunnel is establishing (`Connecting`/`CoreStarted` — a fetch then would be routed
  direct and report the real IP), re-fetches on every arrival at a stable tunnel state
  (`Connected`/`Disconnected`), and retries only failed fetches (3 × 1s; the first answer is
  trusted — the connector drops the connection pool on tunnel-up/disconnect and the tunnel-bound
  client has no keep-alive, so a stale-route answer can't happen). It exposes
  `StateFlow<NetworkData?>`; the dashboard VM only maps it to `NetworkDataUi`. Breaking vs 1.5.1:
  VM constructor takes `NetworkInfoUpdater` instead of `NetworkStateMonitor`, `retryAttempt` is
  gone from `DashboardScreenState`. Don't re-add ad-hoc `getIpData()` triggers to ViewModels.
  Unit-tested (`core_vpn/src/test/.../connectivity/NetworkInfoUpdaterTest.kt`).
- **Auto-retry enrollment on network restore (`core_vpn` 1.2.4, Nexus).**
  `UserInitializer` takes a `NetworkStateMonitor` (breaking constructor change; constructed only in
  `based`'s `AppModule`) and re-runs `enroll()` when the network reconnects while `status` is
  `Failed` / `NotEnrolled` — the dashboard error card self-heals. `Banned` / `VersionOutdated` are
  deliberately not retried; an `InitStatus.Failed` card heals too (mirror discovery re-runs on the
  next SDK call — `HttpClientInitializer` drops its cached client on failure). core_vpn 1.2.4 and
  `based` 1.5.1 (built against it) are on Nexus since 2026-07-18.
- **The reference `:app` bundles the native engine** (`app/libs/libv2ray*.aar` +
  `libhev-socks5-tunnel.so`) — an integration requirement of `v2ray`, separate
  from the `v2ray` library version. `app/libs` is **git-ignored** (same policy as
  `vpn-v2ray`): binaries are not committed — copy them from `vpn-v2ray/app/libs/`
  after cloning, matching the version in `app/build.gradle` (26.5.9).
- **Publish order matters** (commons → core_vpn → based); publishing out of order
  resolves stale transitive versions.
- **Planned refactoring: dashboard VM layering.** A review of `DashboardScreenViewModel`
  (2026-07-18) found four pieces of domain logic to extract; #1 (IP-fetch retry policy) shipped
  as `NetworkInfoUpdater` in `core_vpn` 1.3.0 (see above). Still pending: rating state machine,
  ad/subscription gating, destination-change reconnect orchestration. Read
  `SolarLabs/docs/dashboard-vm-review.md` before touching the dashboard VM.
