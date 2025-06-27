package io.github.skyious.oas.data.model

import kotlinx.serialization.*
import io.github.skyious.oas.util.MapStringAnySerializer

/**
 * Full metadata for an app, parsed from YAML.
 * Fields are nullable if missing. Additional fields are kept in `extraFields`.
 */
@Serializable
enum class SourceType {
    DEFAULT,
    GITHUB,
    FDROID,
    CUSTOM
}

@Serializable
data class AppDetail(
    val id: String? = null,                 // Often a unique ID, sometimes the package name
    val name: String? = null,               // This is likely what you intend for "title"
    val packageName: String? = null,        // Or maybe this IS the primary ID.
    val author: String? = null,            // Making it nullable if not always present
    val summary: String? = null,
    val description: String?,               // Sometimes used instead of or with summary
    val logoUrl: String?,
    val downloadUrl: String,
    val version: String? = null,
    val source: SourceType? = null,
    val images: List<String> = emptyList(),
    val changelog: String? = null,
    @Serializable(with = MapStringAnySerializer::class)
    val otherFields: Map<String, Any?> = emptyMap()
) {
    companion object {
        // These keys are handled as direct properties in AppDetail and should be excluded from the 'otherFields' map.
        val MAIN_KEYS = setOf(
            "id", "name", "packagename", "author", "summary", "description",
            "logourl", "downloadurl", "version", "source", "images",
            "screenshots", "changelog"
        )
    }
}

@Serializable
data class AppInfo(
    val id: String? = null,                 // Often a unique ID, sometimes the package name
    val name: String,                       // This is likely what you intend for "title"
    val packageName: String? = null,        // Or maybe this IS the primary ID.
    val author: String? = null,            // Making it nullable if not always present
    val summary: String? = null,
    val description: String? = null,       // Sometimes used instead of or with summary
    val logoUrl: String?,
    val downloadUrl: String? = null,
    val version: String? = null,
    val source: String? = null,
    val images: List<String>? = emptyList(),
    val changelog: String? = null,
    @Serializable(with = MapStringAnySerializer::class)
    val otherFields: Map<String, Any?>? = emptyMap(),
    val apkName: String? = null,
    val sourceCodeUrl: String? = null,
    val websiteUrl: String? = null,
    val license: String? = null,
    val lastUpdated: Long? = null,
    val dateAdded: Long? = null,
    val configUrl: String? = null,
    val metadataUrl: String? = null,
    val categories: List<String>? = null,
    val suggestedVersionCode: String? = null,
    val iconPath: String? = null
)
