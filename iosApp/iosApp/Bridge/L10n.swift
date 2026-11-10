import Foundation
import Shared

/// Shared strings for SwiftUI: `L10n.text(.loginTitle)`. Replaces Dhruva iOS's `AppStrings`;
/// the same text (English + Hindi) is used by the Android app.
@MainActor
enum L10n {
    private static var localizer: Localizer?

    static func configure(localizer: Localizer) {
        self.localizer = localizer
    }

    static func text(_ key: StringKey) -> String {
        localizer?.text(key: key) ?? key.name
    }

    static func format(_ key: StringKey, _ args: Any...) -> String {
        localizer?.format(key: key, args: args) ?? key.name
    }

    static func resolve(_ text: UiText) -> String {
        localizer?.resolve(text: text) ?? ""
    }
}
