import SwiftUI
import Shared

/// My Digital Addresses: offline-first list, search, pull to refresh, offline banner.
/// Replaces Dhruva iOS's MyDAView data logic with the shared MyAddressesViewModel.
struct MyAddressesScreen: View {
    let graph: AppGraph
    @State private var isCreating = false
    @State private var toast: String?

    init(graph: AppGraph) {
        self.graph = graph
    }

    var body: some View {
        WithViewModel(make: { graph.makeMyAddressesViewModel() }, state: { $0.stateForSwift }) { vm, state in
            NavigationStack {
                VStack(spacing: 0) {
                    if state.canSimulateOffline {
                        Toggle(L10n.text(.devSimulateOffline), isOn: Binding(
                            get: { state.isSimulatingOffline },
                            set: { vm.onSimulateOfflineChange(enabled: $0) }
                        ))
                        .font(.footnote)
                        .padding(.horizontal, 16)
                    }
                    if state.isOffline {
                        Text(L10n.text(.myDaOfflineBanner))
                            .font(.footnote)
                            .foregroundStyle(DhruvaColor.warning)
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(DhruvaColor.warningFill, in: RoundedRectangle(cornerRadius: 12))
                            .padding(16)
                    }
                    content(vm: vm, state: state)
                }
                .background(DhruvaColor.background)
                .navigationTitle(L10n.text(.myDaTitle))
                .searchable(text: Binding(get: { state.query }, set: { vm.onQueryChange(value: $0) }),
                            prompt: L10n.text(.myDaSearchPlaceholder))
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        Button { isCreating = true } label: {
                            Label(L10n.text(.myDaCreate), systemImage: "plus")
                        }
                    }
                }
                .sheet(isPresented: $isCreating) {
                    CreateAddressSheet(graph: graph) { message in
                        isCreating = false
                        if let message { toast = L10n.resolve(message) }
                    }
                }
                .overlay(alignment: .bottom) {
                    if let toast {
                        Text(toast)
                            .font(.footnote)
                            .padding(12)
                            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
                            .padding(16)
                            .task {
                                try? await Task.sleep(for: .seconds(4))
                                self.toast = nil
                            }
                    }
                }
            }
            .tint(DhruvaColor.primary)
            .onAppear { vm.onAppear() }
        }
    }

    @ViewBuilder
    private func content(vm: MyAddressesViewModel, state: MyAddressesUiState) -> some View {
        if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if let error = state.error {
            VStack(spacing: 16) {
                Text(L10n.resolve(error)).multilineTextAlignment(.center)
                PrimaryButton(title: L10n.text(.retry)) { vm.onRetry() }
            }
            .padding(32)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if state.isEmpty {
            VStack(spacing: 12) {
                Text(L10n.text(.myDaEmptyTitle)).font(.headline).multilineTextAlignment(.center)
                Text(L10n.text(.myDaEmptyDescription)).foregroundStyle(DhruvaColor.caption).multilineTextAlignment(.center)
            }
            .padding(32)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if let noResults = state.noResults {
            Text(L10n.resolve(noResults))
                .foregroundStyle(DhruvaColor.caption)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            List(state.addresses, id: \.id) { address in
                AddressRow(address: address)
                    .listRowBackground(Color.white)
            }
            .scrollContentBackground(.hidden)
            .refreshable { vm.onRefresh() }
        }
    }
}

private struct AddressRow: View {
    let address: DigitalAddress

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: address.type == AddressType.home ? "house.fill" : "mappin.circle.fill")
                .foregroundStyle(DhruvaColor.primary)
                .frame(width: 44, height: 44)
                .background(DhruvaColor.cardFill, in: Circle())
            VStack(alignment: .leading, spacing: 2) {
                Text(address.title).font(.headline)
                Text(address.handle).font(.subheadline).foregroundStyle(DhruvaColor.caption)
                Text(L10n.text(address.type.label)).font(.caption).foregroundStyle(DhruvaColor.caption)
                if address.isPendingSync {
                    Label(L10n.text(.myDaPendingSync), systemImage: "arrow.triangle.2.circlepath")
                        .font(.caption)
                        .foregroundStyle(DhruvaColor.warning)
                }
            }
            Spacer()
            Text(L10n.text(.myDaView)).foregroundStyle(DhruvaColor.primary)
        }
        .padding(.vertical, 4)
    }
}
