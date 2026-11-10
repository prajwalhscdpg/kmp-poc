# Dhruva KMP — proof of concept

Kotlin Multiplatform version of the Dhruva (Digital Address Platform) apps. **Domain, data, network,
offline storage, ViewModels, validation, errors and strings are shared**; the UI stays native —
Jetpack Compose on Android, SwiftUI on iOS.

It ports one vertical slice from the two existing apps (`danaiaandroid`, `dan-aia-ios`) so every
layer is exercised end to end:

**Splash → Intro → Login → OTP → Name (new users) → Welcome → My Digital Addresses (offline list, search, create)**

---

## 1. Architecture

```
┌──────────────── androidApp (Compose) ───────┐   ┌──────────── iosApp (SwiftUI) ───────────┐
│ Screens, navigation, theme, WorkManager      │   │ Views, router, theme, Keychain          │
└───────────────┬──────────────────────────────┘   └──────────────┬──────────────────────────┘
                │   AppGraph.make…ViewModel()  ·  state / events  │
┌───────────────▼─────────────────── shared (Kotlin) ─────────────▼──────────────────────────┐
│ presentation  Shared ViewModels: UiState (StateFlow) + one-off events                      │
│ domain        Models, repository interfaces, rules (phone, OTP, name, email, DIGIPIN, …)   │
│ data          Repository implementations, mappers, outbox processor                        │
│ core          network (Ktor) · database (Room KMP) · datastore · session · strings · config │
└────────────────────────────────────────────────────────────────────────────────────────────┘
```

Same MVVM + Clean Architecture as both apps: dependencies point toward `domain`; `data` implements
the domain interfaces. `di/AppGraph.kt` is the single composition root (the KMP counterpart of
iOS's `AppDependencyContainer`) — no DI framework, so Kotlin and Swift use it the same way.

### What is shared, what is native

| Shared (`shared/`) | Native |
|---|---|
| ViewModels and screen state, error timing, countdowns | Layout, components, animation |
| Validation: mobile number, OTP, full name, email, handle prefix | Navigation (NavHost / NavigationStack) |
| All strings, **English + Hindi** (`core/strings`) | Theme and brand colours |
| Typed errors and how they read (`DanAiaError → UiText`) | Map view and location permission (next slice) |
| Ktor client: auth header, single-flight token refresh, `urn:dhruva:app:*` envelopes, timeouts, idempotency keys | Secure storage primitive: Android Keystore (Kotlin), iOS Keychain (Swift) |
| Room KMP cache (source of truth), outbox, sync metadata | Background trigger: WorkManager / app foreground |
| DataStore preferences (language) | |
| DIGIPIN encode/decode, MapTiler geocoding | |
| Mock backend (Ktor MockEngine) | |

### Key files

| Area | File |
|---|---|
| Composition root | `shared/src/commonMain/.../di/AppGraph.kt` |
| Strings | `core/strings/StringKey.kt`, `EnglishStrings.kt`, `HindiStrings.kt`, `Strings.kt` |
| Errors / results | `core/common/DanAiaError.kt`, `DanAiaResult.kt` |
| Network | `core/network/DhruvaHttp.kt`, `TokenRefresher.kt`, `ApiModels.kt`, `MockDhruvaBackend.kt` |
| Offline | `core/database/*`, `data/address/AddressRepositoryImpl.kt`, `OutboxProcessor.kt` |
| Session | `core/session/SessionManager.kt`, `TokenStore.kt` |
| Swift bridge | `shared/.../core/common/FlowBridge.kt` ↔ `iosApp/iosApp/Bridge/KmpBridge.swift` |
| Android VM host | `androidApp/.../ui/common/SharedViewModels.kt` |

### How the UIs consume shared ViewModels

- **Android**: `rememberSharedViewModel { graph.makeLoginViewModel() }` wraps it in a Jetpack
  ViewModel (survives rotation, cleared with the nav entry); state via `collectAsStateWithLifecycle()`.
- **iOS**: `WithViewModel(make: { graph.makeLoginViewModel() }, state: { $0.stateForSwift })` creates it
  once per view, mirrors the `StateFlow` into `@Observable` state and clears it on deinit. Events via
  `.onKmpEvent(vm.eventsForSwift) { … }`. No SKIE / KMP-NativeCoroutines plugin needed.
- Text: Android `text(StringKey.LoginTitle)`, iOS `L10n.text(.loginTitle)` — same table.

### Offline behaviour

| | Behaviour |
|---|---|
| My DAs | Read from Room (instant, works offline). Refreshed on open if older than 15 min, and on pull-to-refresh. Offline with a cache → banner; offline with nothing cached → error + Retry. |
| 404 `NoAddressesFound` | An empty list, not an error (Dhruva iOS / OpenAPI spec). |
| Create address | Saved locally as "Waiting to sync" + outbox row with an `Idempotency-Key`; sent immediately, or replayed later (Android: WorkManager when the network returns; iOS: app foreground; both: before every refresh). The mock backend honours the key, so a retry never duplicates. |
| 401 | One refresh (mutex, "already refreshed?" check). Refused refresh → session ends, local data wiped, both apps go to Login. |

---

## 2. Running

Prerequisites: Android Studio with the KMP plugin, Xcode 26, JDK 17+.

```bash
./gradlew :androidApp:installDebug            # Android app on a device/emulator
./gradlew :shared:testAndroidHostTest          # shared tests on the JVM
./gradlew :shared:iosSimulatorArm64Test        # the same tests on the iOS simulator
```

iOS: open `iosApp/iosApp.xcodeproj` and run (the build phase compiles the `Shared` framework).

### Demo script (mock backend, no server needed)

1. Log in with **9900195577**, OTP **111111** → returning user, three addresses.
   Any other valid number → new user: name screen, welcome, empty list.
2. On My DAs, switch on **Simulate offline** → pull to refresh → offline banner, cached list stays.
3. Still offline, tap **Create** → the address appears as "Waiting to sync".
4. Switch offline off → it syncs and gets its server id.
5. Profile → switch to **हिन्दी**: both apps change language from the same strings.
6. Profile → Sign out → local cache and tokens are wiped.

The mock expires each access token after 5 calls, so the refresh path runs during normal use.
To use a real backend: `AppConfig(useMockBackend = false, baseUrl = …)` in `DhruvaApplication` / `iOSApp`.

---

## 3. Decisions to confirm

The two apps disagree on a few rules; the shared code picks one so they can't drift again.

| Rule | Android | iOS | Shared now |
|---|---|---|---|
| OTP resend countdown | 30 s | 10 s | **30 s** (`OtpPolicy`) |
| Name max length | 100 | 50 | **50** (`FullNameFormatter`) |
| Name characters | anything | letters only | letters (any script), space, `. - '` |
| Who sends the OTP | OTP screen on open | TODO in Login | **Login**, so send errors show next to the number |
| Name screen title | "What is your name?" | "Tell us more about you" | Android's |
| Offline | Room, offline-first | cloud-only, no cache | **offline-first on both** |
| Handle prefix rule | none | none | placeholder: `a-z 0-9 .`, ≤ 30 (`ValidateAddressPrefixUseCase`) |

Hindi for strings that only existed on iOS (My DAs, Create DA, errors) is a first draft for the
translation pipeline. Endpoints other than `GET /app/v1/address` are placeholders
(`ApiPaths`) until the backend publishes them.

---

## 4. Not in this slice / next steps

- **Create DA map wizard** — the shared `CreateAddressViewModel` already covers DIGIPIN, debounced
  search, reverse geocoding, details, handle validation and the offline submit; the native map
  screens (MapLibre on iOS, a map on Android) and current-location permission are next. The POC
  uses a quick form on the default pin.
- Profile suite, Activity History, Home and Sharing tabs (Android's domain rules such as
  `ValidateEmailUseCase` are already ported).
- SMS User Consent autofill on Android (iOS gets OTP autofill via `.oneTimeCode`).
- Split `shared` into Gradle modules (`core`, `domain`, `data`, `feature-*`) once the slice is agreed;
  packages are already laid out that way.
- Room migration tests, iOS `BGTaskScheduler` for background outbox draining.

`_to_delete/` holds the template's Compose Multiplatform sample files; delete it once the build is green.
