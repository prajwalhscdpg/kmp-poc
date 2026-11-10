package com.cdpg.dhruva.core.strings

/**
 * Every user-visible string in the shared slice. One key, one English and one Hindi value
 * (see [EnglishStrings], [HindiStrings]); both apps read the same text from here, so iOS and
 * Android can no longer drift apart.
 *
 * Templates use Android-style positional arguments: `%1$s` for text, `%1$d` for numbers.
 */
enum class StringKey {
    // Common
    AppName,
    ContinueAction,
    Back,
    Retry,
    Close,
    Unavailable,
    SignOut,

    // Errors (from DanAiaError)
    ErrorNetwork,
    ErrorTimeout,
    ErrorSessionExpired,
    ErrorServer,
    ErrorUnexpected,

    // Splash
    SplashInitiativeBy,
    SplashMinistry,

    // Intro (first screen after the splash)
    IntroHeadline,
    IntroSupportingText,
    IntroGetStarted,
    IntroReturningUser,

    // Login
    LoginTagline,
    LoginTitle,
    LoginCountryCode,
    LoginPhonePlaceholder,
    LoginErrorInvalidNumber,
    LoginErrorInvalidStart,

    // OTP
    OtpTitle,
    OtpCodeSent,
    OtpNotReceived,
    OtpResend,
    OtpResendIn,
    OtpErrorIncorrect,

    // Full name
    FullNameWelcome,
    FullNameTitle,
    FullNamePlaceholder,

    // Welcome (new user)
    WelcomeUserGreeting,
    WelcomeUserCreateAddress,
    WelcomeUserSkip,
    WelcomeUserGetStarted,

    // Tabs
    TabHome,
    TabMyAddresses,
    TabSharing,
    TabProfile,

    // My Digital Addresses
    MyDaTitle,
    MyDaSearchPlaceholder,
    MyDaEmptyTitle,
    MyDaEmptyDescription,
    MyDaNoSearchResults,
    MyDaCreate,
    MyDaView,
    MyDaPendingSync,
    MyDaOfflineBanner,
    DevSimulateOffline,

    // Address types
    AddressTypeHome,
    AddressTypeWork,
    AddressTypeOther,

    // Create Digital Address
    CreateDaHeadline,
    CreateDaPreviewNicknamePlaceholder,
    CreateDaPreviewHandlePlaceholder,
    CreateDaDigipinUnavailable,
    CreateDaLocationPermissionDenied,
    CreateDaLocationUnavailable,
    CreateDaNicknameRequired,
    CreateDaPrefixInvalid,
    CreateDaHandleTaken,
    CreateDaQueuedOffline,
    CreateDaFloorGround,
    CreateDaFloorNumbered,
    CreateDaNicknameLabel,
    CreateDaPrefixLabel,
    CreateDaTypeLabel,
    CreateDaDigipinLabel,
    CreateDaSave,
    CreateDaPocNote,

    // Profile / placeholders
    ProfileAppLanguage,
    PlaceholderComingSoon,
}
