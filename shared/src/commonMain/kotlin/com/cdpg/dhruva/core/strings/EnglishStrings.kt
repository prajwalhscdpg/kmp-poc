package com.cdpg.dhruva.core.strings

import com.cdpg.dhruva.core.strings.StringKey.*

/** English. Wording is taken from the Figma-backed copy in both Dhruva apps. */
internal val EnglishStrings: Map<StringKey, String> = mapOf(
    APP_NAME to "Dhruva",
    CONTINUE_ACTION to "Continue",
    BACK to "Back",
    RETRY to "Retry",
    CLOSE to "Close",
    UNAVAILABLE to "—",
    SIGN_OUT to "Sign out",

    ERROR_NETWORK to "No internet connection. Check your connection and try again.",
    ERROR_TIMEOUT to "This is taking longer than usual. Please try again.",
    ERROR_SESSION_EXPIRED to "Your session has expired. Please log in again.",
    ERROR_SERVER to "Something went wrong on our side. Please try again in a little while.",
    ERROR_UNEXPECTED to "Something went wrong. Please try again.",

    SPLASH_INITIATIVE_BY to "An initiative by",
    SPLASH_MINISTRY to "Department of Posts, Ministry of Communications, Government of India",

    INTRO_HEADLINE to "Your Address,\nSimplified.",
    INTRO_SUPPORTING_TEXT to "Create a secure digital address that you can easily manage and share with trusted contacts and organizations.",
    INTRO_GET_STARTED to "Get started",
    INTRO_RETURNING_USER to "Returning user? Log in",

    LOGIN_TAGLINE to "Your Address, Simplified.",
    LOGIN_TITLE to "Log in or Sign up",
    LOGIN_COUNTRY_CODE to "+91",
    LOGIN_PHONE_PLACEHOLDER to "Enter phone number",
    LOGIN_ERROR_INVALID_NUMBER to "Please enter a valid 10-digit mobile number.",
    LOGIN_ERROR_INVALID_START to "Mobile number must start with 6 to 9.",

    OTP_TITLE to "OTP Verification",
    OTP_CODE_SENT to "We have sent a verification code to your phone number ending with %1\$s.",
    OTP_NOT_RECEIVED to "Didn’t get the OTP?",
    OTP_RESEND to "Resend",
    OTP_RESEND_IN to "Resend in %1\$ds",
    OTP_ERROR_INCORRECT to "Incorrect OTP, please enter the correct OTP.",

    FULL_NAME_WELCOME to "Welcome to Dhruva",
    FULL_NAME_TITLE to "What is your name?",
    FULL_NAME_PLACEHOLDER to "Enter your full name",

    WELCOME_USER_GREETING to "Welcome %1\$s.",
    WELCOME_USER_CREATE_ADDRESS to "Let’s create your first\nDigital Address.",
    WELCOME_USER_SKIP to "Skip for now",
    WELCOME_USER_GET_STARTED to "Get started",

    TAB_HOME to "Home",
    TAB_MY_ADDRESSES to "My DAs",
    TAB_SHARING to "Sharing",
    TAB_PROFILE to "Profile",

    MY_DA_TITLE to "My Digital Addresses",
    MY_DA_SEARCH_PLACEHOLDER to "Search your addresses",
    MY_DA_EMPTY_TITLE to "You have no digital addresses yet.\nTap on “Create” to create one.",
    MY_DA_EMPTY_DESCRIPTION to "Digital addresses created by you will show up here. Once created they can be shared with trusted contacts, or organisations.",
    MY_DA_NO_SEARCH_RESULTS to "No addresses match “%1\$s”.",
    MY_DA_CREATE to "Create",
    MY_DA_VIEW to "View",
    MY_DA_PENDING_SYNC to "Waiting to sync",
    MY_DA_OFFLINE_BANNER to "You’re offline. Showing your saved addresses.",
    DEV_SIMULATE_OFFLINE to "Simulate offline (mock backend)",

    ADDRESS_TYPE_HOME to "Home",
    ADDRESS_TYPE_WORK to "Work",
    ADDRESS_TYPE_OTHER to "Other",

    CREATE_DA_HEADLINE to "Create your Digital Address",
    CREATE_DA_PREVIEW_NICKNAME_PLACEHOLDER to "Address Nickname",
    CREATE_DA_PREVIEW_HANDLE_PLACEHOLDER to "prefix@suffix",
    CREATE_DA_DIGIPIN_UNAVAILABLE to "Outside DIGIPIN coverage area",
    CREATE_DA_LOCATION_PERMISSION_DENIED to "Location access is off. Enable it in Settings to use your current location.",
    CREATE_DA_LOCATION_UNAVAILABLE to "Couldn't get your current location. Make sure location is available and try again.",
    CREATE_DA_NICKNAME_REQUIRED to "Give your address a nickname.",
    CREATE_DA_PREFIX_INVALID to "Use letters, numbers and dots only.",
    CREATE_DA_HANDLE_TAKEN to "This Digital Address is already taken. Try a different prefix.",
    CREATE_DA_QUEUED_OFFLINE to "Saved. Your Digital Address will be created as soon as you’re back online.",
    CREATE_DA_FLOOR_GROUND to "Ground",
    CREATE_DA_FLOOR_NUMBERED to "Floor %1\$d",
    CREATE_DA_NICKNAME_LABEL to "Address Nickname",
    CREATE_DA_PREFIX_LABEL to "Address Prefix",
    CREATE_DA_TYPE_LABEL to "Address Type",
    CREATE_DA_DIGIPIN_LABEL to "DIGIPIN",
    CREATE_DA_SAVE to "Create address",
    CREATE_DA_POC_NOTE to "Quick form for the KMP POC: it uses the default map pin (Malleshwaram, Bengaluru). The full map wizard is the next slice.",

    PROFILE_APP_LANGUAGE to "App Language",
    PLACEHOLDER_COMING_SOON to "This tab isn’t part of the KMP POC yet.",
)
