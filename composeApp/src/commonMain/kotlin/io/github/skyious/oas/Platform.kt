package io.github.skyious.oas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform