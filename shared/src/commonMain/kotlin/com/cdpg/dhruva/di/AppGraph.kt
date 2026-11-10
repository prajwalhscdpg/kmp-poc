package com.cdpg.dhruva.di

import com.cdpg.dhruva.core.common.AppClock
import com.cdpg.dhruva.core.config.AppConfig
import com.cdpg.dhruva.core.database.DhruvaDatabase
import com.cdpg.dhruva.core.database.buildDhruvaDatabase
import com.cdpg.dhruva.core.database.wipeUserData
import com.cdpg.dhruva.core.datastore.PreferencesStore
import com.cdpg.dhruva.core.datastore.createPreferencesDataStore
import com.cdpg.dhruva.core.network.DhruvaHttp
import com.cdpg.dhruva.core.network.DhruvaJson
import com.cdpg.dhruva.core.network.MockDhruvaBackend
import com.cdpg.dhruva.core.network.TokenRefresher
import com.cdpg.dhruva.core.network.createDhruvaHttpClient
import com.cdpg.dhruva.core.network.createThirdPartyHttpClient
import com.cdpg.dhruva.core.platform.PlatformDependencies
import com.cdpg.dhruva.core.session.SessionManager
import com.cdpg.dhruva.core.session.TokenStore
import com.cdpg.dhruva.core.strings.AppLanguage
import com.cdpg.dhruva.core.strings.Localizer
import com.cdpg.dhruva.data.address.AddressRepositoryImpl
import com.cdpg.dhruva.data.address.OutboxProcessor
import com.cdpg.dhruva.data.auth.AuthRepositoryImpl
import com.cdpg.dhruva.data.location.GeocodingRepositoryImpl
import com.cdpg.dhruva.data.location.MapTilerGeocodingClient
import com.cdpg.dhruva.data.profile.ProfileRepositoryImpl
import com.cdpg.dhruva.domain.address.AddressRepository
import com.cdpg.dhruva.domain.address.SearchAddressesUseCase
import com.cdpg.dhruva.domain.address.ValidateAddressPrefixUseCase
import com.cdpg.dhruva.domain.auth.AuthRepository
import com.cdpg.dhruva.domain.auth.FullNameFormatter
import com.cdpg.dhruva.domain.auth.ValidateMobileNumberUseCase
import com.cdpg.dhruva.domain.location.GeocodingRepository
import com.cdpg.dhruva.domain.profile.ProfileRepository
import com.cdpg.dhruva.presentation.addresses.MyAddressesViewModel
import com.cdpg.dhruva.presentation.auth.FullNameViewModel
import com.cdpg.dhruva.presentation.auth.LoginViewModel
import com.cdpg.dhruva.presentation.auth.OtpViewModel
import com.cdpg.dhruva.presentation.createaddress.CreateAddressViewModel
import com.cdpg.dhruva.presentation.splash.SplashViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The single composition root for the shared code, the KMP counterpart of Dhruva iOS's
 * AppDependencyContainer: it builds every object once and hands out `make…ViewModel()`
 * factories. No DI framework, so it reads the same from Kotlin and Swift.
 *
 * Create exactly one per process: Android in `DhruvaApplication`, iOS in `iOSApp`.
 */
class AppGraph(
    private val platform: PlatformDependencies,
    val config: AppConfig,
) {
    /** Lives as long as the process; for background work such as outbox draining. */
    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val clock: AppClock = AppClock.System

    // Storage

    private val database: DhruvaDatabase by lazy { buildDhruvaDatabase(platform.databaseBuilder()) }

    val preferences: PreferencesStore by lazy {
        PreferencesStore(
            dataStore = createPreferencesDataStore(platform.preferencesFilePath()),
            deviceLanguage = AppLanguage.fromTag(platform.deviceLanguageTag()),
            scope = appScope,
        )
    }

    val localizer: Localizer by lazy { Localizer(preferences.language) }

    val session: SessionManager by lazy {
        SessionManager(
            tokenStore = TokenStore(platform.secureStorage, DhruvaJson),
            wipeLocalData = { database.wipeUserData() },
        )
    }

    // Network

    /** Non-null when [AppConfig.useMockBackend]: the in-process backend the app talks to. */
    val mockBackend: MockDhruvaBackend? by lazy {
        if (config.useMockBackend) MockDhruvaBackend(clock) else null
    }

    private val dhruvaHttp: DhruvaHttp by lazy {
        val engine = mockBackend?.engine() ?: platform.httpEngine()
        val client = createDhruvaHttpClient(engine, config) { preferences.language.value.tag }
        DhruvaHttp(client, session, TokenRefresher(client, session))
    }

    private val geocodingClient: MapTilerGeocodingClient by lazy {
        MapTilerGeocodingClient(createThirdPartyHttpClient(platform.httpEngine()), config.mapTilerApiKey)
    }

    // Data

    val outbox: OutboxProcessor by lazy { OutboxProcessor(dhruvaHttp, database) }

    private val authRepository: AuthRepository by lazy { AuthRepositoryImpl(dhruvaHttp, session) }
    private val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl(dhruvaHttp) }
    private val addressRepository: AddressRepository by lazy {
        AddressRepositoryImpl(dhruvaHttp, database, outbox, clock)
    }
    private val geocodingRepository: GeocodingRepository by lazy { GeocodingRepositoryImpl(geocodingClient) }

    // Domain rules (stateless)

    private val validateMobileNumber = ValidateMobileNumberUseCase()
    private val fullNameFormatter = FullNameFormatter()
    private val searchAddresses = SearchAddressesUseCase()
    private val validatePrefix = ValidateAddressPrefixUseCase()

    // ViewModel factories (one new instance per screen)

    fun makeSplashViewModel(): SplashViewModel = SplashViewModel(session)

    fun makeLoginViewModel(): LoginViewModel = LoginViewModel(validateMobileNumber, authRepository)

    fun makeOtpViewModel(phoneNumber: String): OtpViewModel = OtpViewModel(phoneNumber, authRepository)

    fun makeFullNameViewModel(): FullNameViewModel = FullNameViewModel(fullNameFormatter, profileRepository)

    fun makeMyAddressesViewModel(): MyAddressesViewModel =
        MyAddressesViewModel(addressRepository, searchAddresses, session, mockBackend)

    fun makeCreateAddressViewModel(): CreateAddressViewModel =
        CreateAddressViewModel(geocodingRepository, addressRepository, validatePrefix)

    // Fire-and-forget actions for Swift (Kotlin suspend functions are awkward from SwiftUI views).

    fun setLanguage(language: AppLanguage) {
        appScope.launch { preferences.setLanguage(language) }
    }

    fun signOut() {
        appScope.launch { session.signOut() }
    }

    // Background sync

    /** Replays queued writes. Android's OutboxWorker awaits [drainOutbox]; iOS calls this on foreground. */
    fun drainOutboxInBackground() {
        if (!session.isSignedIn) return
        appScope.launch(Dispatchers.IO) { outbox.drain() }
    }

    suspend fun drainOutbox(): Int = if (session.isSignedIn) outbox.drain() else 0
}
