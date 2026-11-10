import SwiftUI
import Shared

/// The tab shell (Home, My DAs, Sharing, Profile) from both apps.
struct MainTabView: View {
    let graph: AppGraph
    @State private var selection = 1

    init(graph: AppGraph) {
        self.graph = graph
    }

    var body: some View {
        TabView(selection: $selection) {
            ComingSoonView(title: L10n.text(.tabHome))
                .tabItem { Label(L10n.text(.tabHome), systemImage: "house") }
                .tag(0)
            MyAddressesScreen(graph: graph)
                .tabItem { Label(L10n.text(.tabMyAddresses), systemImage: "mappin.and.ellipse") }
                .tag(1)
            ComingSoonView(title: L10n.text(.tabSharing))
                .tabItem { Label(L10n.text(.tabSharing), systemImage: "arrow.left.arrow.right") }
                .tag(2)
            ProfileTabView(graph: graph)
                .tabItem { Label(L10n.text(.tabProfile), systemImage: "person") }
                .tag(3)
        }
        .tint(DhruvaColor.primary)
    }
}

private struct ComingSoonView: View {
    let title: String

    var body: some View {
        VStack(spacing: 8) {
            Text(title).font(.title2)
            Text(L10n.text(.placeholderComingSoon)).foregroundStyle(DhruvaColor.caption).multilineTextAlignment(.center)
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(DhruvaColor.background)
    }
}

/// Language switch (proves shared strings at runtime) and sign out (proves the session wipe).
private struct ProfileTabView: View {
    let graph: AppGraph
    @State private var language: KmpState<AppLanguage>

    init(graph: AppGraph) {
        self.graph = graph
        _language = State(initialValue: KmpState(graph.preferences.languageForSwift))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(L10n.text(.tabProfile)).font(.largeTitle.weight(.semibold))
            Text(L10n.text(.profileAppLanguage)).font(.headline)
            Picker(L10n.text(.profileAppLanguage), selection: Binding(
                get: { language.value },
                set: { graph.setLanguage(language: $0) }
            )) {
                Text("English").tag(AppLanguage.english)
                Text("हिन्दी").tag(AppLanguage.hindi)
            }
            .pickerStyle(.segmented)
            SecondaryButton(title: L10n.text(.signOut)) { graph.signOut() }
            Spacer()
        }
        .padding(24)
        .background(DhruvaColor.background)
    }
}
