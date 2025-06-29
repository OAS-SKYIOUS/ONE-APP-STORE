package io.github.skyious.oas.fdroid.data.model

import kotlinx.datetime.Instant

/**
 * Represents an F-Droid repository.
 *
 * @property id A unique identifier for the repository.
 * @property address The URL of the repository.
 * @property name The display name of the repository.
 * @property timestamp The last time the repository index was successfully fetched.
 * @property enabled Whether the repository is currently active.
 */
data class Repo(
    val id: Long = 0,
    val address: String,
    val name: String,
    val timestamp: Instant,
    val enabled: Boolean = true
)
