package com.cdpg.dhruva.core.strings

import com.cdpg.dhruva.core.strings.StringKey.*

/**
 * Hindi. Login/OTP/name/welcome/splash text is from Dhruva Android's values-hi. The rest
 * (My DAs, Create DA, errors — strings that only existed on iOS, in English) is a first draft
 * for the translation pipeline to review. A missing key falls back to English.
 */
internal val HindiStrings: Map<StringKey, String> = mapOf(
    APP_NAME to "ध्रुव",
    CONTINUE_ACTION to "आगे बढ़ें",
    BACK to "वापस",
    RETRY to "फिर से कोशिश करें",
    CLOSE to "बंद करें",
    UNAVAILABLE to "—",
    SIGN_OUT to "साइन आउट करें",

    ERROR_NETWORK to "इंटरनेट कनेक्शन नहीं है। अपना कनेक्शन जांचें और फिर से कोशिश करें।",
    ERROR_TIMEOUT to "इसमें सामान्य से अधिक समय लग रहा है। कृपया फिर से कोशिश करें।",
    ERROR_SESSION_EXPIRED to "आपका सत्र समाप्त हो गया है। कृपया फिर से लॉग इन करें।",
    ERROR_SERVER to "हमारी ओर से कुछ गड़बड़ हो गई। कृपया थोड़ी देर बाद फिर से कोशिश करें।",
    ERROR_UNEXPECTED to "कुछ गड़बड़ हो गई। कृपया फिर से कोशिश करें।",

    SPLASH_INITIATIVE_BY to "एक पहल",
    SPLASH_MINISTRY to "डाक विभाग, संचार मंत्रालय, भारत सरकार",

    INTRO_HEADLINE to "आपका पता,\nआसान।",
    INTRO_SUPPORTING_TEXT to "एक सुरक्षित डिजिटल पता बनाएं जिसे आप आसानी से प्रबंधित कर सकें और भरोसेमंद संपर्कों व संगठनों के साथ साझा कर सकें।",
    INTRO_GET_STARTED to "शुरू करें",
    INTRO_RETURNING_USER to "पहले से उपयोगकर्ता हैं? लॉग इन करें",

    LOGIN_TAGLINE to "आपका पता, आसान।",
    LOGIN_TITLE to "लॉग इन या साइन अप करें",
    LOGIN_COUNTRY_CODE to "+91",
    LOGIN_PHONE_PLACEHOLDER to "फ़ोन नंबर दर्ज करें",
    LOGIN_ERROR_INVALID_NUMBER to "कृपया मान्य 10 अंकों का मोबाइल नंबर दर्ज करें।",
    LOGIN_ERROR_INVALID_START to "मोबाइल नंबर 6 से 9 से शुरू होना चाहिए।",

    OTP_TITLE to "OTP सत्यापन",
    OTP_CODE_SENT to "हमने %1\$s पर समाप्त होने वाले आपके फ़ोन नंबर पर एक सत्यापन कोड भेजा है।",
    OTP_NOT_RECEIVED to "OTP नहीं मिला?",
    OTP_RESEND to "फिर से भेजें",
    OTP_RESEND_IN to "%1\$d सेकंड में फिर से भेजें",
    OTP_ERROR_INCORRECT to "गलत OTP, कृपया सही OTP दर्ज करें।",

    FULL_NAME_WELCOME to "ध्रुव में आपका स्वागत है",
    FULL_NAME_TITLE to "आपका नाम क्या है?",
    FULL_NAME_PLACEHOLDER to "अपना पूरा नाम दर्ज करें",

    WELCOME_USER_GREETING to "स्वागत है, %1\$s।",
    WELCOME_USER_CREATE_ADDRESS to "आइए अपना पहला\nडिजिटल पता बनाएं।",
    WELCOME_USER_SKIP to "अभी छोड़ें",
    WELCOME_USER_GET_STARTED to "शुरू करें",

    TAB_HOME to "होम",
    TAB_MY_ADDRESSES to "मेरे पते",
    TAB_SHARING to "साझा करना",
    TAB_PROFILE to "प्रोफ़ाइल",

    MY_DA_TITLE to "मेरे डिजिटल पते",
    MY_DA_SEARCH_PLACEHOLDER to "अपने पते खोजें",
    MY_DA_EMPTY_TITLE to "आपके पास अभी कोई डिजिटल पता नहीं है।\nबनाने के लिए “बनाएं” पर टैप करें।",
    MY_DA_EMPTY_DESCRIPTION to "आपके बनाए डिजिटल पते यहां दिखेंगे। बनने के बाद इन्हें भरोसेमंद संपर्कों या संगठनों के साथ साझा किया जा सकता है।",
    MY_DA_NO_SEARCH_RESULTS to "“%1\$s” से कोई पता मेल नहीं खाता।",
    MY_DA_CREATE to "बनाएं",
    MY_DA_VIEW to "देखें",
    MY_DA_PENDING_SYNC to "सिंक होना बाकी है",
    MY_DA_OFFLINE_BANNER to "आप ऑफ़लाइन हैं। आपके सहेजे गए पते दिखाए जा रहे हैं।",
    DEV_SIMULATE_OFFLINE to "ऑफ़लाइन का अनुकरण करें (मॉक बैकएंड)",

    ADDRESS_TYPE_HOME to "घर",
    ADDRESS_TYPE_WORK to "कार्यस्थल",
    ADDRESS_TYPE_OTHER to "अन्य",

    CREATE_DA_HEADLINE to "अपना डिजिटल पता बनाएं",
    CREATE_DA_PREVIEW_NICKNAME_PLACEHOLDER to "पते का उपनाम",
    CREATE_DA_PREVIEW_HANDLE_PLACEHOLDER to "prefix@suffix",
    CREATE_DA_DIGIPIN_UNAVAILABLE to "DIGIPIN कवरेज क्षेत्र से बाहर",
    CREATE_DA_LOCATION_PERMISSION_DENIED to "लोकेशन की अनुमति बंद है। अपनी मौजूदा लोकेशन इस्तेमाल करने के लिए सेटिंग में इसे चालू करें।",
    CREATE_DA_LOCATION_UNAVAILABLE to "आपकी मौजूदा लोकेशन नहीं मिल सकी। लोकेशन उपलब्ध है यह सुनिश्चित करें और फिर से कोशिश करें।",
    CREATE_DA_NICKNAME_REQUIRED to "अपने पते को एक उपनाम दें।",
    CREATE_DA_PREFIX_INVALID to "केवल अक्षर, अंक और बिंदु इस्तेमाल करें।",
    CREATE_DA_HANDLE_TAKEN to "यह डिजिटल पता पहले से लिया जा चुका है। कोई दूसरा प्रीफ़िक्स आज़माएं।",
    CREATE_DA_QUEUED_OFFLINE to "सहेजा गया। आपके ऑनलाइन होते ही आपका डिजिटल पता बना दिया जाएगा।",
    CREATE_DA_FLOOR_GROUND to "भूतल",
    CREATE_DA_FLOOR_NUMBERED to "मंज़िल %1\$d",
    CREATE_DA_NICKNAME_LABEL to "पते का उपनाम",
    CREATE_DA_PREFIX_LABEL to "पते का प्रीफ़िक्स",
    CREATE_DA_TYPE_LABEL to "पते का प्रकार",
    CREATE_DA_DIGIPIN_LABEL to "DIGIPIN",
    CREATE_DA_SAVE to "पता बनाएं",
    CREATE_DA_POC_NOTE to "KMP POC के लिए छोटा फ़ॉर्म: यह मैप के डिफ़ॉल्ट पिन (मल्लेश्वरम, बेंगलुरु) का उपयोग करता है। पूरा मैप विज़ार्ड अगले चरण में है।",

    PROFILE_APP_LANGUAGE to "ऐप की भाषा",
    PLACEHOLDER_COMING_SOON to "यह टैब अभी KMP POC का हिस्सा नहीं है।",
)
