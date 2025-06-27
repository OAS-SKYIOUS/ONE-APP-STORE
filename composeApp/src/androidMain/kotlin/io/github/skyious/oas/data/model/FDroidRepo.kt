package io.github.skyious.oas.data.model

data class FDroidRepo(
    val name: String,
    val address: String,
    val publicKey: String? = null,
    val archiveUrl: String? = null
)
