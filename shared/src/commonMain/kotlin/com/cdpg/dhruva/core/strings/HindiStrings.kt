package com.cdpg.dhruva.core.strings

import com.cdpg.dhruva.core.strings.StringKey.*

/**
 * Hindi. Login/OTP/name/welcome/splash text is from Dhruva Android's values-hi. The rest
 * (My DAs, Create DA, errors — strings that only existed on iOS, in English) is a first draft
 * for the translation pipeline to review. A missing key falls back to English.
 */
internal val HindiStrings: Map<StringKey, String> = mapOf(
    AppName to "ध्रुव",
    ContinueAction to "आगे बढ़ें",
    Back to "वापस",
    Retry to "फिर से कोशिश करें",
    Close to "बंद करें",
    Unavailable to "—",
    SignOut to "साइन आउट करें",

    ErrorNetwork to "इंटरनेट कनेक्शन नहीं है। अपना कनेक्शन जांचें और फिर से कोशिश करें।",
    ErrorTimeout to "इसमें सामान्य से अधिक समय लग रहा है। कृपया फिर से कोशिश करें।",
    ErrorSessionExpired to "आपका सत्र समाप्त हो गया है। कृपया फिर से लॉग इन करें।",
    ErrorServer to "हमारी ओर से कुछ गड़बड़ हो गई। कृपया थोड़ी देर बाद फिर से कोशिश करें।",
    ErrorUnexpected to "कुछ गड़बड़ हो गई। कृपया फिर से कोशिश करें।",

    SplashInitiativeBy to "एक पहल",
    SplashMinistry to "डाक विभाग, संचार मंत्रालय, भारत सरकार",

    IntroHeadline to "आपका पता,\nआसान।",
    IntroSupportingText to "एक सुरक्षित डिजिटल पता बनाएं जिसे आप आसानी से प्रबंधित कर सकें और भरोसेमंद संपर्कों व संगठनों के साथ साझा कर सकें।",
    IntroGetStarted to "शुरू करें",
    IntroReturningUser to "पहले से उपयोगकर्ता हैं? लॉग इन करें",

    LoginTagline to "आपका पता, आसान।",
    LoginTitle to "लॉग इन या साइन अप करें",
    LoginCountryCode to "+91",
    LoginPhonePlaceholder to "फ़ोन नंबर दर्ज करें",
    LoginErrorInvalidNumber to "कृपया मान्य 10 अंकों का मोबाइल नंबर दर्ज करें।",
    LoginErrorInvalidStart to "मोबाइल नंबर 6 से 9 से शुरू होना चाहिए।",

    OtpTitle to "OTP सत्यापन",
    OtpCodeSent to "हमने %1\$s पर समाप्त होने वाले आपके फ़ोन नंबर पर एक सत्यापन कोड भेजा है।",
    OtpNotReceived to "OTP नहीं मिला?",
    OtpResend to "फिर से भेजें",
    OtpResendIn to "%1\$d सेकंड में फिर से भेजें",
    OtpErrorIncorrect to "गलत OTP, कृपया सही OTP दर्ज करें।",

    FullNameWelcome to "ध्रुव में आपका स्वागत है",
    FullNameTitle to "आपका नाम क्या है?",
    FullNamePlaceholder to "अपना पूरा नाम दर्ज करें",

    WelcomeUserGreeting to "स्वागत है, %1\$s।",
    WelcomeUserCreateAddress to "आइए अपना पहला\nडिजिटल पता बनाएं।",
    WelcomeUserSkip to "अभी छोड़ें",
    WelcomeUserGetStarted to "शुरू करें",

    TabHome to "होम",
    TabMyAddresses to "मेरे पते",
    TabSharing to "साझा करना",
    TabProfile to "प्रोफ़ाइल",

    MyDaTitle to "मेरे डिजिटल पते",
    MyDaSearchPlaceholder to "अपने पते खोजें",
    MyDaEmptyTitle to "आपके पास अभी कोई डिजिटल पता नहीं है।\nबनाने के लिए “बनाएं” पर टैप करें।",
    MyDaEmptyDescription to "आपके बनाए डिजिटल पते यहां दिखेंगे। बनने के बाद इन्हें भरोसेमंद संपर्कों या संगठनों के साथ साझा किया जा सकता है।",
    MyDaNoSearchResults to "“%1\$s” से कोई पता मेल नहीं खाता।",
    MyDaCreate to "बनाएं",
    MyDaView to "देखें",
    MyDaPendingSync to "सिंक होना बाकी है",
    MyDaOfflineBanner to "आप ऑफ़लाइन हैं। आपके सहेजे गए पते दिखाए जा रहे हैं।",
    DevSimulateOffline to "ऑफ़लाइन का अनुकरण करें (मॉक बैकएंड)",

    AddressTypeHome to "घर",
    AddressTypeWork to "कार्यस्थल",
    AddressTypeOther to "अन्य",

    CreateDaHeadline to "अपना डिजिटल पता बनाएं",
    CreateDaPreviewNicknamePlaceholder to "पते का उपनाम",
    CreateDaPreviewHandlePlaceholder to "prefix@suffix",
    CreateDaDigipinUnavailable to "DIGIPIN कवरेज क्षेत्र से बाहर",
    CreateDaLocationPermissionDenied to "लोकेशन की अनुमति बंद है। अपनी मौजूदा लोकेशन इस्तेमाल करने के लिए सेटिंग में इसे चालू करें।",
    CreateDaLocationUnavailable to "आपकी मौजूदा लोकेशन नहीं मिल सकी। लोकेशन उपलब्ध है यह सुनिश्चित करें और फिर से कोशिश करें।",
    CreateDaNicknameRequired to "अपने पते को एक उपनाम दें।",
    CreateDaPrefixInvalid to "केवल अक्षर, अंक और बिंदु इस्तेमाल करें।",
    CreateDaHandleTaken to "यह डिजिटल पता पहले से लिया जा चुका है। कोई दूसरा प्रीफ़िक्स आज़माएं।",
    CreateDaQueuedOffline to "सहेजा गया। आपके ऑनलाइन होते ही आपका डिजिटल पता बना दिया जाएगा।",
    CreateDaFloorGround to "भूतल",
    CreateDaFloorNumbered to "मंज़िल %1\$d",
    CreateDaNicknameLabel to "पते का उपनाम",
    CreateDaPrefixLabel to "पते का प्रीफ़िक्स",
    CreateDaTypeLabel to "पते का प्रकार",
    CreateDaDigipinLabel to "DIGIPIN",
    CreateDaSave to "पता बनाएं",
    CreateDaPocNote to "KMP POC के लिए छोटा फ़ॉर्म: यह मैप के डिफ़ॉल्ट पिन (मल्लेश्वरम, बेंगलुरु) का उपयोग करता है। पूरा मैप विज़ार्ड अगले चरण में है।",

    ProfileAppLanguage to "ऐप की भाषा",
    PlaceholderComingSoon to "यह टैब अभी KMP POC का हिस्सा नहीं है।",
)
