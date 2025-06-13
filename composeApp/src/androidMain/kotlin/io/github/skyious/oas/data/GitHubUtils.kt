package io.github.skyious.oas.data


import android.net.Uri

object GitHubUtils {
    /**
     * Given a GitHub repo URL like "https://github.com/username/repo" or
     * "https://github.com/username/repo.git", returns Pair(owner, repoName), or null if invalid.
     */
    fun parseOwnerRepo(repoUrl: String): Pair<String, String>? {
        return try {
            val uri = Uri.parse(repoUrl.trim())
            // Expect host contains "github.com"
            if (uri.host?.contains("github.com") != true) return null
            // Path segments: ["", "username", "repo"] or sometimes trailing slash
            val segments = uri.pathSegments
            if (segments.size >= 2) {
                val owner = segments[0]
                var repo = segments[1]
                if (repo.endsWith(".git")) {
                    repo = repo.removeSuffix(".git")
                }
                if (owner.isNotBlank() && repo.isNotBlank()) {
                    owner to repo
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Build raw content URL base for a given owner/repo/branch.
     * E.g., owner="username", repo="reponame", branch="main" ->
     * "https://raw.githubusercontent.com/username/reponame/main/"
     */
    fun rawBase(owner: String, repo: String, branch: String = "main"): String =
        "https://raw.githubusercontent.com/$owner/$repo/$branch/"

    /**
     * Build GitHub API URL to list contents at a path.
     * E.g., owner, repo, path="apps" -> "https://api.github.com/repos/owner/repo/contents/apps?ref=main"
     */
    fun apiContentsUrl(owner: String, repo: String, path: String, branch: String = "main"): String =
        "https://api.github.com/repos/$owner/$repo/contents/$path?ref=$branch"
}
