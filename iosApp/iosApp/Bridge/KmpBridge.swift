import SwiftUI
import Shared

// SwiftUI ⇄ shared ViewModels. The shared code exposes `CStateFlow` / `CFlow` with a `watch`
// callback (shared/core/common/FlowBridge.kt); these types turn them into Observation state.

/// Holds a shared ViewModel and mirrors its state for SwiftUI. Clears the ViewModel on deinit.
@MainActor
@Observable
final class KmpViewModel<VM: SharedViewModel, UiState: AnyObject> {
    let vm: VM
    private(set) var state: UiState
    @ObservationIgnored private var handle: WatchHandle?

    init(_ vm: VM, state: (VM) -> CStateFlow<UiState>) {
        self.vm = vm
        let flow = state(vm)
        self.state = flow.value
        handle = flow.watch { [weak self] value in self?.state = value }
    }

    deinit {
        handle?.close()
        vm.clear()
    }
}

/// Creates the ViewModel once per view identity (not on every struct re-init) and renders it.
struct WithViewModel<VM: SharedViewModel, UiState: AnyObject, Content: View>: View {
    let make: () -> VM
    let state: (VM) -> CStateFlow<UiState>
    let content: (VM, UiState) -> Content

    @State private var model: KmpViewModel<VM, UiState>?

    init(
        make: @escaping () -> VM,
        state: @escaping (VM) -> CStateFlow<UiState>,
        @ViewBuilder content: @escaping (VM, UiState) -> Content
    ) {
        self.make = make
        self.state = state
        self.content = content
    }

    var body: some View {
        Group {
            if let model {
                content(model.vm, model.state)
            } else {
                Color.clear
            }
        }
        .onAppear {
            if model == nil { model = KmpViewModel(make(), state: state) }
        }
    }
}

/// Mirrors any shared `CStateFlow` (session state, language…) as observable state.
@MainActor
@Observable
final class KmpState<T: AnyObject> {
    private(set) var value: T
    @ObservationIgnored private var handle: WatchHandle?

    init(_ flow: CStateFlow<T>) {
        value = flow.value
        handle = flow.watch { [weak self] in self?.value = $0 }
    }

    deinit { handle?.close() }
}

/// Delivers one-off events (navigate, show message) from a shared `CFlow` while the view is on screen.
private struct KmpEvents<Event: AnyObject>: ViewModifier {
    let flow: () -> CFlow<Event>
    let handler: (Event) -> Void
    @State private var handle: WatchHandle?

    func body(content: Content) -> some View {
        content
            .onAppear {
                if handle == nil { handle = flow().watch { handler($0) } }
            }
            .onDisappear {
                handle?.close()
                handle = nil
            }
    }
}

extension View {
    func onKmpEvent<Event: AnyObject>(_ flow: @autoclosure @escaping () -> CFlow<Event>, perform handler: @escaping (Event) -> Void) -> some View {
        modifier(KmpEvents(flow: flow, handler: handler))
    }
}
