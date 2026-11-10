package com.cdpg.dhruva.domain.profile

import com.cdpg.dhruva.core.common.DanAiaResult

interface ProfileRepository {
    suspend fun updateFullName(fullName: String): DanAiaResult<Unit>
}

/**
 * The optional email on Personal details (ported from Dhruva Android): empty is allowed,
 * anything else must look like `name@domain.tld`. The server does the strict check.
 */
class ValidateEmailUseCase {

    operator fun invoke(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isEmpty() || (trimmed.length <= MAX_LENGTH && EMAIL.matches(trimmed))
    }

    private companion object {
        /** RFC 5321 limit for a whole address. */
        const val MAX_LENGTH = 254
        val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    }
}
