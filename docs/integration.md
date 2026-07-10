# Publishing & consuming

How to publish these modules in the right order, and the two ways a VPN app
consumes them.

## Publishing

All modules publish to the private NorseLabs Nexus
(`https://nexus.norselabs.dev/repository/maven-releases/`) under
`io.norselabs.vpn:*`. Export credentials first:

```bash
export NORSELABS_REPO_LOGIN=<user>
export NORSELABS_REPO_PASSWORD=<pass>
```

Per module:

```bash
./gradlew :<module>:publishToMavenLocal   # ~/.m2 — test a consumer locally first
./gradlew :<module>:publish               # private Nexus release
```

### Dependency-correct order

Publish bottom-up so each module resolves already-published versions of what it
depends on:

```
1. :common  :common_logger  :common_flags  :common_map  :common_net_apps  :common_referral
2. :common_purchases  :common_compose        # need :common
3. :common_purchases_ui                       # needs :common_purchases
4. :core_vpn                                  # needs :common + v2ray + dvpn_sdk
5. :based_vpn  (artifact `based`)             # needs :core_vpn + commons + v2ray + dvpn_sdk
```

The foundation libraries `io.norselabs.vpn:v2ray` and `:dvpn_sdk` live in their
own repos and must be published before `:core_vpn` / `:based_vpn` can build
against a new version. The SolarLabs top-level `scripts/publish-all.sh` automates
this whole chain across repos.

### Versioning

- Each module's version is the `version` in its `publishing { }` block
  (`<module>/build.gradle`). Bump only what changed.
- After publishing, bump the version in each consumer's
  `gradle/libs.versions.toml`.
- `based` re-exports `v2ray` / `dvpn_sdk` / `core_vpn` with `api`, so bumping a
  foundation lib usually means republishing `core_vpn` and `based` too.

## Consuming

Add the Nexus repo (credentials from env) in the consumer's
`settings.gradle(.kts)`, then choose a pattern.

### Pattern A — thin wrapper (build on `based`)

Depend on `based` and reuse its ViewModels and DI. The app supplies a theme,
resources, and an `AppConfig` implementation; it gets the rest for free. This is
how **bagmisiz-vpn-android** is built.

```groovy
dependencies {
    implementation 'io.norselabs.vpn:based:1.4.2'
    // optional extra UI commons used directly:
    implementation 'io.norselabs.vpn:common_compose:1.2.0'
    implementation 'io.norselabs.vpn:common_map:0.0.4'
}
```

Then implement `AppConfig` and provide it as a Hilt singleton (see
[architecture.md](architecture.md)). `v2ray` / `dvpn_sdk` / `core_vpn` arrive
transitively through `based`.

### Pattern B — building blocks (deep customization)

Skip `based` and depend on the lower-level modules directly, then write your own
ViewModels / navigation / DI. This is how **DVPN** is built.

```groovy
dependencies {
    implementation 'io.norselabs.vpn:core_vpn:1.2.2'
    implementation 'io.norselabs.vpn:v2ray:1.1.0'
    implementation 'io.norselabs.vpn:dvpn_sdk:2.2.0'
    implementation 'io.norselabs.vpn:common:0.0.4'
    // + whichever common_* modules you need
}
```

### Native engine (both patterns)

The `v2ray` tunnel engine ships as a native artifact: the consuming app must
bundle `app/libs/libv2ray*.aar` and the `libhev-socks5-tunnel.so` libraries and
configure `jniLibs` packaging. This is an integration requirement of
`io.norselabs.vpn:v2ray` (see that repo's `docs/integration.md`), independent of
the `v2ray` library version.
