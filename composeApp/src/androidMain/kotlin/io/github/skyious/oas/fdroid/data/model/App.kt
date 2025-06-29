package io.github.skyious.oas.fdroid.data.model

/**
 * Represents an application from an F-Droid repository.
 *
 * @property packageName The unique package name of the application (e.g., "org.fdroid.fdroid").
 * @property name The display name of the application.
 * @property summary A short description of the application.
 * @property description A full description of the application.
 * @property icon The filename of the application's icon.
 * @property addedDate The date the application was added to the repository.
 * @property lastUpdatedDate The date the application was last updated.
 * @property authorName The name of the application author.
 * @property webUrl A URL to the application's website.
 * @property sourceCodeUrl A URL to the application's source code.
 * @property issueTrackerUrl A URL to the application's issue tracker.
 * @property license The license under which the application is distributed.
 * @property antiFeatures A list of anti-features associated with the application.
 * @property categories A list of categories the application belongs to.
 */
data class App(
    val packageName: String,
    val name: String,
    val summary: String?,
    val description: String?,
    val icon: String?,
    val addedDate: Long,
    val lastUpdatedDate: Long,
    val authorName: String?,
    val webUrl: String?,
    val sourceCodeUrl: String?,
    val issueTrackerUrl: String?,
    val license: String?,
    val antiFeatures: List<String> = emptyList(),
    val categories: List<String> = emptyList()
)
