package io.github.skyious.oas.fdroid.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoV2(
    val timestamp: Long,
    val version: Long,
    val name: String,
    val address: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("icon")
    val icon: String? = null
)
