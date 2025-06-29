package io.github.skyious.oas.fdroid.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class App(
    val packageName: String,
    val name: String,
    val summary: String,
    val description: String,
    val icon: String?,
    val featureGraphic: String?,
    val screenshots: List<String> = emptyList(),
    val author: String,
    val lastUpdated: Long,
    val added: Long,
    val categories: List<String> = emptyList(),
    val sourceCode: String?,
    val issueTracker: String?,
    val license: String,
    val versions: List<Version> = emptyList()
)

@Serializable
data class Version(
    val versionCode: Long,
    val versionName: String,
    val size: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val permissions: List<String> = emptyList(),
    val url: String
)
