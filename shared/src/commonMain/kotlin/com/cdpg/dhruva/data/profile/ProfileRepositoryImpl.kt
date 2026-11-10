package com.cdpg.dhruva.data.profile

import io.ktor.client.request.url
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.network.ApiPaths
import com.cdpg.dhruva.core.network.DhruvaHttp
import com.cdpg.dhruva.core.network.UpdateProfileRequest
import com.cdpg.dhruva.core.network.apiCall
import com.cdpg.dhruva.core.network.requireSuccess
import com.cdpg.dhruva.domain.profile.ProfileRepository
import com.cdpg.dhruva.core.network.jsonBody
import io.ktor.http.HttpMethod

internal class ProfileRepositoryImpl(private val http: DhruvaHttp) : ProfileRepository {

    override suspend fun updateFullName(fullName: String): DanAiaResult<Unit> = apiCall {
        http.execute(authenticated = true) {
            method = HttpMethod.Patch
            url(ApiPaths.PROFILE)
            jsonBody(UpdateProfileRequest(fullName))
        }.requireSuccess()
    }
}
