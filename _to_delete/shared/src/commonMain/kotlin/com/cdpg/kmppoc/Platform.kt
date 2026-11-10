package com.cdpg.kmppoc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform