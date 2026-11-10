package com.cdpg.dhruva.domain.auth

import com.cdpg.dhruva.core.common.DanAiaResult

/** Mobile number login. On a verified OTP the session is started (tokens stored securely). */
interface AuthRepository {

    /** Sends a new OTP by SMS to [phoneNumber] (10 digits, no +91). */
    suspend fun requestOtp(phoneNumber: String): DanAiaResult<Unit>

    suspend fun verifyOtp(phoneNumber: String, otp: String): DanAiaResult<OtpVerification>
}

sealed class OtpVerification {
    /** Signed in. [isNewUser] decides whether to ask for the name next. */
    data class Verified(val isNewUser: Boolean) : OtpVerification()

    /** The code doesn't match the one sent. */
    data object Incorrect : OtpVerification()
}
