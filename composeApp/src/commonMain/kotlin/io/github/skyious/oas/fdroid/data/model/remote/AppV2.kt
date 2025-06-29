package io.github.skyious.oas.fdroid.data.model.remote

import io.github.skyious.oas.fdroid.domain.model.App
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppV2(
    @SerialName("packageName")
    val packageName: String,
    @SerialName("name")
    val name: Map<String, String> = emptyMap(),
    @SerialName("summary")
    val summary: Map<String, String> = emptyMap(),
    @SerialName("description")
    val description: Map<String, String> = emptyMap(),
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("added")
    val added: Long,
    @SerialName("lastUpdated")
    val lastUpdated: Long,
    @SerialName("authorName")
    val authorName: String? = null,
    @SerialName("webSite")
    val webUrl: String? = null,
    @SerialName("sourceCode")
    val sourceCodeUrl: String? = null,
    @SerialName("issueTracker")
    val issueTrackerUrl: String? = null,
    @SerialName("license")
    val license: String? = null,
    @SerialName("antiFeatures")
    val antiFeatures: Map<String, String> = emptyMap(),
    @SerialName("categories")
    val categories: List<String> = emptyList()
)

/**
 * Maps the remote [AppV2] data model to the domain [App] model.
 * It selects the English ("en-US") localization for text fields as a default.
 */
fun AppV2.toDomain(repoAddress: String): App {
    val defaultLocale = "en-US"

    // Icon URLs must be relative to the repo root, not a localized path.
    // Strip any potential locale from the end of the repo address.
    val repoBaseUrl = repoAddress.removeSuffix("/$defaultLocale").removeSuffix("/")

    // Construct the full URL for the app icon.
    val iconUrl = icon?.let { iconName ->
        "$repoBaseUrl/icons-640/$iconName"
    } ?: "$repoBaseUrl/$packageName/icon.png" // Corrected fallback URL

    // This log helps confirm the generated URL is correct.
    println("FDROID_URL_DEBUG App: $packageName, Author: $authorName, IconName: $icon, Final URL: $iconUrl")

    return App(
        packageName = this.packageName,
        name = this.name[defaultLocale] ?: this.packageName,
        summary = this.summary[defaultLocale] ?: "",
        description = this.description[defaultLocale] ?: "",
        icon = iconUrl,
        featureGraphic = null, // Not available in V2
        screenshots = emptyList(), // Not available in V2
        author = this.authorName ?: "Unknown author",
        lastUpdated = this.lastUpdated,
        added = this.added,
        categories = this.categories,
        sourceCode = this.sourceCodeUrl,
        issueTracker = this.issueTrackerUrl,
        license = this.license ?: "Unknown",
        versions = emptyList() // Not available in V2, needs to be populated later
    )
}
