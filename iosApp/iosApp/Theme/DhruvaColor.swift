import SwiftUI
import Shared

/// A few Dhruva brand tokens (Dhruva iOS DESIGN_SYSTEM.md).
enum DhruvaColor {
    static let primary = Color(red: 0xAA / 255, green: 0x4A / 255, blue: 0x44 / 255)
    static let cardFill = Color(red: 0xF0 / 255, green: 0xE1 / 255, blue: 0xDC / 255)
    static let background = Color(red: 0xF8 / 255, green: 0xF2 / 255, blue: 0xED / 255)
    static let backgroundTop = Color(red: 0xF1 / 255, green: 0xE4 / 255, blue: 0xDA / 255)
    static let onSurface = Color(red: 0x1D / 255, green: 0x1B / 255, blue: 0x20 / 255)
    static let caption = Color(red: 0x49 / 255, green: 0x45 / 255, blue: 0x4F / 255)
    static let inputBorder = Color(red: 0xB8 / 255, green: 0x95 / 255, blue: 0x7A / 255)
    static let warning = Color(red: 0x8A / 255, green: 0x5A / 255, blue: 0x00 / 255)
    static let warningFill = Color(red: 1, green: 0xF0 / 255, blue: 0xD1 / 255)

    static let gradient = LinearGradient(colors: [backgroundTop, background], startPoint: .top, endPoint: .bottom)
}

struct PrimaryButton: View {
    let title: String
    var isEnabled = true
    var isLoading = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView().tint(.white)
                } else {
                    Text(title).fontWeight(.medium)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 52)
            .background(DhruvaColor.primary.opacity(isEnabled ? 1 : 0.4), in: Capsule())
            .foregroundStyle(.white)
        }
        .disabled(!isEnabled || isLoading)
    }
}

struct SecondaryButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .fontWeight(.medium)
                .frame(maxWidth: .infinity, minHeight: 52)
                .overlay(Capsule().stroke(DhruvaColor.inputBorder))
                .foregroundStyle(DhruvaColor.primary)
        }
    }
}

struct BrandMark: View {
    var body: some View {
        Text(L10n.text(.appName))
            .font(.system(size: 40, weight: .bold))
            .foregroundStyle(DhruvaColor.primary)
    }
}

struct InitiativeFooter: View {
    var body: some View {
        VStack(spacing: 4) {
            Text(L10n.text(.splashInitiativeBy)).font(.caption).foregroundStyle(DhruvaColor.caption)
            Text(L10n.text(.splashMinistry)).font(.footnote).foregroundStyle(DhruvaColor.onSurface)
        }
        .multilineTextAlignment(.center)
    }
}

/// White rounded card used by the auth screens.
struct AuthCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 16) { content }
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(.white, in: RoundedRectangle(cornerRadius: 24))
    }
}

/// The red line under a field, from a shared `UiText` error.
struct ErrorLine: View {
    let text: UiText?

    var body: some View {
        if let text {
            Text(L10n.resolve(text)).font(.footnote).foregroundStyle(.red)
        }
    }
}

/// A text field whose value is owned by a shared ViewModel, which may clean up what was typed
/// (digits only, collapsed spaces…). `read` returns the cleaned value right after `write`.
struct SharedTextField: View {
    let placeholder: String
    let value: String
    let read: () -> String
    let write: (String) -> Void
    let keyboard: UIKeyboardType
    let contentType: UITextContentType?
    let hasError: Bool
    let onSubmit: () -> Void

    @State private var text = ""

    init(
        placeholder: String,
        value: String,
        read: @escaping () -> String,
        write: @escaping (String) -> Void,
        keyboard: UIKeyboardType = .default,
        contentType: UITextContentType? = nil,
        hasError: Bool = false,
        onSubmit: @escaping () -> Void = {}
    ) {
        self.placeholder = placeholder
        self.value = value
        self.read = read
        self.write = write
        self.keyboard = keyboard
        self.contentType = contentType
        self.hasError = hasError
        self.onSubmit = onSubmit
    }

    var body: some View {
        TextField(placeholder, text: $text)
            .keyboardType(keyboard)
            .textContentType(contentType)
            .onSubmit(onSubmit)
            .padding(14)
            .background(.white, in: RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(hasError ? Color.red : DhruvaColor.inputBorder))
            .onAppear { text = value }
            .onChange(of: text) { _, new in
                guard new != value else { return }
                write(new)
                let cleaned = read()
                if cleaned != new { text = cleaned }
            }
            .onChange(of: value) { _, new in
                if new != text { text = new }
            }
    }
}
