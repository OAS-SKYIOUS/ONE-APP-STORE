package io.github.skyious.oas.data.model

/**
 * Full metadata for an app, parsed from YAML.
 * Fields are nullable if missing. Additional fields are kept in `extraFields`.
 */
enum class SourceType {
    DEFAULT,
    GITHUB,
    FDROID // Assuming FDROID is defined like this
    ,
    CUSTOM
}

data class AppDetail(
    val id: String? = null,                 // Often a unique ID, sometimes the package name
    val name: String? = null,               // This is likely what you intend for "title"
    val packageName: String? = null,        // Or maybe this IS the primary ID.
    val author: String? = null,            // Making it nullable if not always present
    val summary: String? = null,
    val description: String?,       // Sometimes used instead of or with summary
    val logoUrl: String?,
    val downloadUrl: String,
    val version: String? = null,
    val source: SourceType? = null,
    val images: List<String> = emptyList(),
    val changelog: String? = null,
    val otherFields: Map<String, Any?> = emptyMap() // Changed from 'sourceType' to 'source'
    // Add any other fields you have like 'tags', 'license', 'lastUpdated', etc.
)

data class AppInfo(
    val id: String? = null,                 // Often a unique ID, sometimes the package name
    val name: String,               // This is likely what you intend for "title"
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
    val suggestedVersionCode: String? = null, // Example: if you want to store this too
    val iconPath: String? = null // Example
    // Changed from 'sourceType' to 'source'
    // Add any other fields you have like 'tags', 'license', 'lastUpdated', etc.
)
