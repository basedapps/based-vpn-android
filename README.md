# BasedVPN Android

A monorepo of reusable, independently-published Android modules that make up the
NorseLabs VPN platform's high- and mid-level layers. The original BasedVPN app
became this collection of building blocks: every module publishes to the private
NorseLabs Nexus under `io.norselabs.vpn:*` and is consumed by the shipping VPN
apps.

The headline module is **`based`** (`io.norselabs.vpn:based`) — it bundles
ready-made Voyager ViewModels, a Hilt DI graph, and the `AppConfig` contract, so
a new branded VPN app can be built as a thin UI wrapper. Apps that need more
control depend on the lower-level modules (`core_vpn`, the `common_*` libraries)
directly instead.

## Modules

| Module | Artifact | Role |
|---|---|---|
| `based_vpn` | `based` | High-level reuse layer: ViewModels, Hilt DI, `AppConfig` |
| `core_vpn` | `core_vpn` | Low-level VPN orchestration (connector, driver, DNS, split tunneling) |
| `common` | `common` | Shared utils, preferences, state holders |
| `common_logger` | `common_logger` | Timber + file logging + log upload |
| `common_flags` | `common_flags` | Country flag assets |
| `common_map` | `common_map` | Compose map components |
| `common_net_apps` | `common_net_apps` | Installed-apps utilities (split tunneling) |
| `common_referral` | `common_referral` | Branch + Google Ads referral plumbing |
| `common_purchases` | `common_purchases` | RevenueCat purchases logic |
| `common_purchases_ui` | `common_purchases_ui` | RevenueCat paywall UI |
| `common_compose` | `common_compose` | Reusable Compose components |
| `app` | _(not published)_ | The original BasedVPN reference app |

## Publishing

```bash
# Maven Local (~/.m2 — for testing consumers locally)
./gradlew :based_vpn:publishToMavenLocal

# Private Nexus release (needs NORSELABS_REPO_LOGIN / NORSELABS_REPO_PASSWORD)
./gradlew :based_vpn:publish
```

Publish per module (`:core_vpn`, `:common`, …) in dependency order. See
[docs/integration.md](docs/integration.md) for the full order and how consumers
adopt a new version.

## Documentation

- Agent / contributor guide: [CLAUDE.md](CLAUDE.md)
- Architecture & module map: [docs/architecture.md](docs/architecture.md)
- Publishing & consuming: [docs/integration.md](docs/integration.md)
- Conventions: [docs/conventions.md](docs/conventions.md)

minSdk 26 · compileSdk 36 · Kotlin 2.2.20 · AGP 8.13.0 · JVM 17.
