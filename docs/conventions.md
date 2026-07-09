# Conventions

Patterns to follow when working in this monorepo. Prefer existing patterns over
introducing new abstractions.

## Code style

- Enforced by [.editorconfig](.editorconfig): **ktlint** with the `android_studio`
  code style, LF line endings, UTF-8, trailing whitespace trimmed, final newline.
- Match the surrounding code; several ktlint standard rules are intentionally
  disabled in `.editorconfig` — don't re-enable them ad hoc.

## Modules & dependencies

- One responsibility per module; `common_*` modules stay free of internal deps
  where possible (they are the leaves of the graph).
- **All dependency versions go in the version catalog**
  [gradle/libs.versions.toml](gradle/libs.versions.toml) and are referenced via
  `libs.*` — never hardcode versions inline in a `build.gradle`.
- Gradle DSL is **Groovy** (`build.gradle`), not Kotlin DSL.
- A module exposes a downstream dependency with `api` only when consumers need it
  transitively (as `based` does for the foundation libs); otherwise use
  `implementation`.
- Each publishable module declares its coordinates + `version` in a
  `publishing { }` block.

## Dependency injection

- **Hilt** with constructor injection. `@Provides` / `@Binds` live in `di/`
  modules installed in `SingletonComponent`.
- A library module provides its own Hilt modules; the consuming app supplies
  app-specific bindings (notably `AppConfig`).
- KAPT is used for Hilt (AGP pinned below 9 for this reason platform-wide).

## UI & state

- **Voyager** `ScreenModel` ViewModels, injected via Hilt.
- Jetpack Compose for UI; shared components live in `common_compose`.

## Networking & async

- **Ktor** client; results modeled with Arrow `Either` (errors as values, no
  thrown exceptions across boundaries).
- `kotlinx.serialization` for models.
- Coroutines + `Flow` / `StateFlow` for async and observable state.

## Logging

- **Timber**; persisted via `common_logger`'s `FileLogTree`, uploadable via
  `LogsSender`. Never log secrets (app token, credentials) or the backend host.

## VPN behavior — be conservative

The VPN connect/disconnect, DNS, and split-tunneling paths are load-bearing and
easy to break subtly. Read `core_vpn` (`VPNConnector`, `VPNDriver`,
`DnsConfigurator`, `SplitTunnelingConfigurator`) and `based`'s `core_impl/vpn`
before changing them, and verify an actual connect end-to-end after.

## Publishing discipline

Publish in dependency order (commons → `core_vpn` → `based`) and propagate the
new version into each consumer's catalog. See [integration.md](integration.md).
