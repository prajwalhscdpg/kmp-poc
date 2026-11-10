import Foundation
import Security
import Shared

/// The shared `SecureStorage` contract, implemented with the Keychain.
/// Same queries as Dhruva iOS's `DefaultKeychainStorage`, so tokens stay in the Keychain only.
final class KeychainSecureStorage: NSObject, SecureStorage {
    private let service: String

    init(service: String = Bundle.main.bundleIdentifier ?? "com.cdpg.dap") {
        self.service = service
        super.init()
    }

    func read(key: String) -> String? {
        var query = baseQuery(for: key)
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var result: AnyObject?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
              let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func write(key: String, value: String) {
        let data = Data(value.utf8)
        var addQuery = baseQuery(for: key)
        addQuery[kSecValueData as String] = data
        addQuery[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly

        let status = SecItemAdd(addQuery as CFDictionary, nil)
        if status == errSecDuplicateItem {
            let update: [String: Any] = [kSecValueData as String: data]
            SecItemUpdate(baseQuery(for: key) as CFDictionary, update as CFDictionary)
        }
    }

    func delete(key: String) {
        SecItemDelete(baseQuery(for: key) as CFDictionary)
    }

    private func baseQuery(for key: String) -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
    }
}
