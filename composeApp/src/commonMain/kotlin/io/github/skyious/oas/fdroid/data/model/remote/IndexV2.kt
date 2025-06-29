package io.github.skyious.oas.fdroid.data.model.remote

import kotlinx.serialization.Serializable

@Serializable
data class IndexV2(
    val repo: RepoV2,
    val apps: List<AppV2> = emptyList()
)
