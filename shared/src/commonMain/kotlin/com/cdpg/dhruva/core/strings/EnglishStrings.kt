package com.cdpg.dhruva.core.strings

import com.cdpg.dhruva.core.strings.StringKey.*

/** English. Wording is taken from the Figma-backed copy in both Dhruva apps. */
internal val EnglishStrings: Map<StringKey, String> = mapOf(
    AppName to "Dhruva",
    ContinueAction to "Continue",
    Back to "Back",
    Retry to "Retry",
    Close to "Close",
    Unavailable to "—",
    SignOut to "Sign out",

    ErrorNetwork to "No internet connection. Check your connection and try again.",
    ErrorTimeout to "This is taking longer than usual. Please try again.",
    ErrorSessionExpired to "Your session has expired. Please log in again.",
    ErrorServer to "Something went wrong on our side. Please try again in a little while.",
    ErrorUnexpected to "Something went wrong. Please try again.",

    SplashInitiativeBy to "An initiative by",
    SplashMinistry to "Department of Posts, Ministry of Communications, Government of India",

    IntroHeadline to "Your Address,\nSimplified.",
    IntroSupportingText to "Create a secure digital address that you can easily manage and share with trusted contacts and organizations.",
    IntroGetStarted to "Get started",
    IntroReturningUser to "Returning user? Log in",

    LoginTagline to "Your Address, Simplified.",
    LoginTitle to "Log in or Sign up",
    LoginCountryCode to "+91",
    LoginPhonePlaceholder to "Enter phone number",
    LoginErrorInvalidNumber to "Please enter a valid 10-digit mobile number.",
    LoginErrorInvalidStart to "Mobile number must start with 6 to 9.",

    OtpTitle to "OTP Verification",
    OtpCodeSent to "We have sent a verification code to your phone number ending with %1\$s.",
    OtpNotReceived to "Didn’t get the OTP?",
    OtpResend to "Resend",
    OtpResendIn to "Resend in %1\$ds",
    OtpErrorIncorrect to "Incorrect OTP, please enter the correct OTP.",

    FullNameWelcome to "Welcome to Dhruva",
    FullNameTitle to "What is your name?",
    FullNamePlaceholder to "Enter your full name",

    WelcomeUserGreeting to "Welcome %1\$s.",
    WelcomeUserCreateAddress to "Let’s create your first\nDigital Address.",
    WelcomeUserSkip to "Skip for now",
    WelcomeUserGetStarted to "Get started",

    TabHome to "Home",
    TabMyAddresses to "My DAs",
    TabSharing to "Sharing",
    TabProfile to "Profile",

    MyDaTitle to "My Digital Addresses",
    MyDaSearchPlaceholder to "Search your addresses",
    MyDaEmptyTitle to "You have no digital addresses yet.\nTap on “Create” to create one.",
    MyDaEmptyDescription to "Digital addresses created by you will show up here. Once created they can be shared with trusted contacts, or organisations.",
    MyDaNoSearchResults to "No addresses match “%1\$s”.",
    MyDaCreate to "Create",
    MyDaView to "View",
    MyDaPendingSync to "Waiting to sync",
    MyDaOfflineBanner to "You’re offline. Showing your saved addresses.",
    DevSimulateOffline to "Simulate offline (mock backend)",

    AddressTypeHome to "Home",
    AddressTypeWork to "Work",
    AddressTypeOther to "Other",

    CreateDaHeadline to "Create your Digital Address",
    CreateDaPreviewNicknamePlaceholder to "Address Nickname",
    CreateDaPreviewHandlePlaceholder to "prefix@suffix",
    CreateDaDigipinUnavailable to "Outside DIGIPIN coverage area",
    CreateDaLocationPermissionDenied to "Location access is off. Enable it in Settings to use your current location.",
    CreateDaLocationUnavailable to "Couldn't get your current location. Make sure location is available and try again.",
    CreateDaNicknameRequired to "Give your address a nickname.",
    CreateDaPrefixInvalid to "Use letters, numbers and dots only.",
    CreateDaHandleTaken to "This Digital Address is already taken. Try a different prefix.",
    CreateDaQueuedOffline to "Saved. Your Digital Address will be created as soon as you’re back online.",
    CreateDaFloorGround to "Ground",
    CreateDaFloorNumbered to "Floor %1\$d",
    CreateDaNicknameLabel to "Address Nickname",
    CreateDaPrefixLabel to "Address Prefix",
    CreateDaTypeLabel to "Address Type",
    CreateDaDigipinLabel to "DIGIPIN",
    CreateDaSave to "Create address",
    CreateDaPocNote to "Quick form for the KMP POC: it uses the default map pin (Malleshwaram, Bengaluru). The full map wizard is the next slice.",

    ProfileAppLanguage to "App Language",
    PlaceholderComingSoon to "This tab isn’t part of the KMP POC yet.",
)
