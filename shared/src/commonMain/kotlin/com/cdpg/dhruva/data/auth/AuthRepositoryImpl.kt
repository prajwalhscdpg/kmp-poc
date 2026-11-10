package com.cdpg.dhruva.data.auth

import io.ktor.client.request.url
import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.common.ProblemType
import com.cdpg.dhruva.core.network.ApiEnvelope
import com.cdpg.dhruva.core.network.ApiPaths
import com.cdpg.dhruva.core.network.DhruvaHttp
import com.cdpg.dhruva.core.network.SendOtpRequest
import com.cdpg.dhruva.core.network.TokenResults
import com.cdpg.dhruva.core.network.VerifyOtpRequest
import com.cdpg.dhruva.core.network.apiCall
import com.cdpg.dhruva.core.network.bodyOrThrow
import com.cdpg.dhruva.core.network.requireSuccess
import com.cdpg.dhruva.core.session.AuthTokens
import com.cdpg.dhruva.core.session.SessionManager
import com.cdpg.dhruva.domain.auth.AuthRepository
import com.cdpg.dhruva.domain.auth.OtpVerification
import com.cdpg.dhruva.core.network.jsonBody
import io.ktor.http.HttpMethod

internal class AuthRepositoryImpl(
    private val http: DhruvaHttp,
    private val session: SessionManager,
) : AuthRepository {

    override suspend fun requestOtp(phoneNumber: String): DanAiaResult<Unit> = apiCall {
        http.execute(authenticated = false) {
            method = HttpMethod.Post
            url(ApiPaths.SEND_OTP)
            jsonBody(SendOtpRequest(phoneNumber))
        }.requireSuccess()
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): DanAiaResult<OtpVerification> {
        val result = apiCall {
            http.execute(authenticated = false) {
                method = HttpMethod.Post
                url(ApiPaths.VERIFY_OTP)
                jsonBody(VerifyOtpRequest(phoneNumber, otp))
            }.bodyOrThrow<ApiEnvelope<TokenResults>>()
        }
        return when (result) {
            is DanAiaResult.Success -> {
                val tokens = result.data.results ?: return DanAiaResult.Error(DanAiaError.Unexpected)
                session.startSession(AuthTokens(tokens.accessToken, tokens.refreshToken))
                DanAiaResult.Success(OtpVerification.Verified(isNewUser = tokens.isNewUser))
            }
            is DanAiaResult.Error -> {
                val error = result.error
                if (error is DanAiaError.Validation && error.type == ProblemType.INCORRECT_OTP) {
                    DanAiaResult.Success(OtpVerification.Incorrect)
                } else {
                    result
                }
            }
        }
    }
}
