package io.github.skyious.oas.data.model

/**
 * Full metadata for an app, parsed from YAML.
 * Fields are nullable if missing. Additional fields are kept in `extraFields`.
 */
data class AppDetail(
    val name: String?,
    val packageName: String?,   // if provided
    val version: String?,
    val description: String?,
    val downloadUrl: String?,   // main download link
    val logoUrl: String?,       // maybe override or same as AppInfo.logoUrl
    val images: List<String> = emptyList(),  // additional image URLs
    val changelog: String?,     // textual changelog
    val author: String?,
    val otherFields: Map<String, Any> = emptyMap()
)
