package io.github.skyious.oas.data.model

/**
 * Minimal info for listing in Discover screen.
 * - name, author, logoUrl always needed.
 * - configUrl: only for main index apps (points to a YAML/one config file with full metadata).
 * - downloadUrl: for external-source apps, the direct download link parsed from metadata.
 * - source: helps identify origin if you want.
 */
data class AppInfo(
    val name: String,
    val author: String,
    val logoUrl: String,
    val configUrl: String? = null,    // default
    val downloadUrl: String? = null,  // custom
    val metadataUrl: String? = null,  // custom: raw URL to YAML metadata file
    val source: SourceType = SourceType.DEFAULT
)


enum class SourceType {
    DEFAULT,    // from main CSV index
    CUSTOM      // from external listinginfo-based repos
}
