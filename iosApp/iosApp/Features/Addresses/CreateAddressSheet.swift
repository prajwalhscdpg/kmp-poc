import SwiftUI
import Shared

/// POC stand-in for the Create DA wizard: nickname, type and prefix on the default pin.
/// It drives the real shared CreateAddressViewModel (validation, DIGIPIN, offline-capable
/// submit through the outbox); the map steps are the next slice.
struct CreateAddressSheet: View {
    let graph: AppGraph
    let onFinished: (UiText?) -> Void

    var body: some View {
        WithViewModel(make: { graph.makeCreateAddressViewModel() }, state: { $0.stateForSwift }) { vm, state in
            NavigationStack {
                Form {
                    Section {
                        Text(L10n.text(.createDaPocNote)).font(.footnote).foregroundStyle(DhruvaColor.caption)
                        LabeledContent(L10n.text(.createDaDigipinLabel), value: L10n.resolve(state.digipinDisplay))
                    }
                    Section(L10n.text(.createDaTypeLabel)) {
                        Picker(L10n.text(.createDaTypeLabel), selection: Binding(
                            get: { state.type },
                            set: { vm.onTypeSelected(type: $0) }
                        )) {
                            ForEach(state.typeOptions, id: \.self) { type in
                                Text(L10n.text(type.label)).tag(type)
                            }
                        }
                        .pickerStyle(.segmented)
                    }
                    Section(L10n.text(.createDaNicknameLabel)) {
                        SharedTextField(
                            placeholder: L10n.text(.createDaPreviewNicknamePlaceholder),
                            value: state.nickname,
                            read: { vm.stateForSwift.value.nickname },
                            write: { vm.onNicknameChange(value: $0) },
                            hasError: state.nicknameError != nil
                        )
                        ErrorLine(text: state.nicknameError)
                    }
                    Section(L10n.text(.createDaPrefixLabel)) {
                        HStack {
                            SharedTextField(
                                placeholder: "bandra.104",
                                value: state.prefix,
                                read: { vm.stateForSwift.value.prefix },
                                write: { vm.onPrefixChange(value: $0) },
                                keyboard: .asciiCapable,
                                hasError: state.prefixError != nil
                            )
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            Text(state.suffix).foregroundStyle(DhruvaColor.caption)
                        }
                        ErrorLine(text: state.prefixError)
                        Text(L10n.resolve(state.previewHandle)).font(.footnote).foregroundStyle(DhruvaColor.caption)
                    }
                    if let error = state.submitError {
                        Section { ErrorLine(text: error) }
                    }
                    Section {
                        PrimaryButton(title: L10n.text(.createDaSave), isLoading: state.isSubmitting) {
                            vm.onNicknameProceed()
                        }
                    }
                    .listRowBackground(Color.clear)
                }
                .navigationTitle(L10n.text(.createDaHeadline))
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button(L10n.text(.close)) { onFinished(nil) }
                    }
                }
            }
            .onKmpEvent(vm.eventsForSwift) { event in
                // The quick form skips the map and details steps.
                if event is CreateAddressEvent.GoToConfirm {
                    vm.onConfirm()
                } else if let finished = event as? CreateAddressEvent.Finished {
                    onFinished(finished.message)
                }
            }
        }
    }
}
