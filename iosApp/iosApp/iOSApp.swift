import SwiftUI
import Shared

/// Owns the one shared `AppGraph` (the KMP composition root) for the process.
@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    private let graph: AppGraph

    init() {
        let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0"
        // No real backend yet: run against the in-process mock (OTP 111111).
        graph = IosAppGraphKt.createIosAppGraph(
            secureStorage: KeychainSecureStorage(),
            deviceLanguageTag: Locale.current.language.languageCode?.identifier ?? "en",
            config: AppConfigs.shared.mock(appVersion: version, platform: "ios")
        )
        L10n.configure(localizer: graph.localizer)
    }

    var body: some Scene {
        WindowGroup {
            RootView(graph: graph)
        }
        .onChange(of: scenePhase) { _, phase in
            // Foreground: deliver anything the outbox queued while offline (shared logic).
            if phase == .active { graph.drainOutboxInBackground() }
        }
    }
}
