package io.github.skyious.oas.fdroid.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    val id: Long = 0,
    val name: String,
    val address: String,
    val enabled: Boolean = true,
    val lastUpdated: Long = 0
)
