import SwiftUI
import Shared

enum AuthRoute: Hashable {
    case otp(phone: String)
    case fullName
    case welcome(firstName: String)
}

/// Top-level flow, like Dhruva iOS's AppCoordinator: splash → intro → login flow → tabs.
@MainActor
@Observable
final class AppRouter {
    enum Flow { case splash, intro, auth, main }

    var flow: Flow = .splash
    var authPath: [AuthRoute] = []

    func showAuth() {
        authPath = []
        flow = .auth
    }
}

struct RootView: View {
    let graph: AppGraph

    @State private var router = AppRouter()
    @State private var session: KmpState<SessionState>
    @State private var language: KmpState<AppLanguage>

    init(graph: AppGraph) {
        self.graph = graph
        _session = State(initialValue: KmpState(graph.session.stateForSwift))
        _language = State(initialValue: KmpState(graph.preferences.languageForSwift))
    }

    var body: some View {
        Group {
            switch router.flow {
            case .splash:
                SplashScreen(graph: graph) { destination in
                    router.flow = destination == SplashDestination.main ? .main : .intro
                }
            case .intro:
                IntroScreen { router.showAuth() }
            case .auth:
                AuthFlowView(graph: graph, router: router)
            case .main:
                MainTabView(graph: graph)
            }
        }
        // A language change rebuilds the screens so every shared string is read again.
        .id(language.value)
        // Session ended (sign out, or the server refused the refresh token): back to Login.
        .onChange(of: session.value) { _, state in
            if state == SessionState.signedOut && router.flow == .main { router.showAuth() }
        }
    }
}

struct AuthFlowView: View {
    let graph: AppGraph
    @Bindable var router: AppRouter

    var body: some View {
        NavigationStack(path: $router.authPath) {
            LoginScreen(graph: graph) { phone in router.authPath.append(.otp(phone: phone)) }
                .navigationDestination(for: AuthRoute.self) { route in
                    switch route {
                    case .otp(let phone):
                        OtpScreen(graph: graph, phoneNumber: phone) { isNewUser in
                            if isNewUser {
                                router.authPath = [.fullName]
                            } else {
                                router.flow = .main
                            }
                        }
                    case .fullName:
                        FullNameScreen(graph: graph) { firstName in router.authPath.append(.welcome(firstName: firstName)) }
                            .navigationBarBackButtonHidden()
                    case .welcome(let firstName):
                        WelcomeScreen(firstName: firstName) { router.flow = .main }
                            .navigationBarBackButtonHidden()
                    }
                }
        }
        .tint(DhruvaColor.primary)
    }
}
