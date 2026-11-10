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
    APP_NAME,
    CONTINUE_ACTION,
    BACK,
    RETRY,
    CLOSE,
    UNAVAILABLE,
    SIGN_OUT,

    // Errors (from DanAiaError)
    ERROR_NETWORK,
    ERROR_TIMEOUT,
    ERROR_SESSION_EXPIRED,
    ERROR_SERVER,
    ERROR_UNEXPECTED,

    // Splash
    SPLASH_INITIATIVE_BY,
    SPLASH_MINISTRY,

    // Intro (first screen after the splash)
    INTRO_HEADLINE,
    INTRO_SUPPORTING_TEXT,
    INTRO_GET_STARTED,
    INTRO_RETURNING_USER,

    // Login
    LOGIN_TAGLINE,
    LOGIN_TITLE,
    LOGIN_COUNTRY_CODE,
    LOGIN_PHONE_PLACEHOLDER,
    LOGIN_ERROR_INVALID_NUMBER,
    LOGIN_ERROR_INVALID_START,

    // OTP
    OTP_TITLE,
    OTP_CODE_SENT,
    OTP_NOT_RECEIVED,
    OTP_RESEND,
    OTP_RESEND_IN,
    OTP_ERROR_INCORRECT,

    // Full name
    FULL_NAME_WELCOME,
    FULL_NAME_TITLE,
    FULL_NAME_PLACEHOLDER,

    // Welcome (new user)
    WELCOME_USER_GREETING,
    WELCOME_USER_CREATE_ADDRESS,
    WELCOME_USER_SKIP,
    WELCOME_USER_GET_STARTED,

    // Tabs
    TAB_HOME,
    TAB_MY_ADDRESSES,
    TAB_SHARING,
    TAB_PROFILE,

    // My Digital Addresses
    MY_DA_TITLE,
    MY_DA_SEARCH_PLACEHOLDER,
    MY_DA_EMPTY_TITLE,
    MY_DA_EMPTY_DESCRIPTION,
    MY_DA_NO_SEARCH_RESULTS,
    MY_DA_CREATE,
    MY_DA_VIEW,
    MY_DA_PENDING_SYNC,
    MY_DA_OFFLINE_BANNER,
    DEV_SIMULATE_OFFLINE,

    // Address types
    ADDRESS_TYPE_HOME,
    ADDRESS_TYPE_WORK,
    ADDRESS_TYPE_OTHER,

    // Create Digital Address
    CREATE_DA_HEADLINE,
    CREATE_DA_PREVIEW_NICKNAME_PLACEHOLDER,
    CREATE_DA_PREVIEW_HANDLE_PLACEHOLDER,
    CREATE_DA_DIGIPIN_UNAVAILABLE,
    CREATE_DA_LOCATION_PERMISSION_DENIED,
    CREATE_DA_LOCATION_UNAVAILABLE,
    CREATE_DA_NICKNAME_REQUIRED,
    CREATE_DA_PREFIX_INVALID,
    CREATE_DA_HANDLE_TAKEN,
    CREATE_DA_QUEUED_OFFLINE,
    CREATE_DA_FLOOR_GROUND,
    CREATE_DA_FLOOR_NUMBERED,
    CREATE_DA_NICKNAME_LABEL,
    CREATE_DA_PREFIX_LABEL,
    CREATE_DA_TYPE_LABEL,
    CREATE_DA_DIGIPIN_LABEL,
    CREATE_DA_SAVE,
    CREATE_DA_POC_NOTE,

    // Profile / placeholders
    PROFILE_APP_LANGUAGE,
    PLACEHOLDER_COMING_SOON,
}
