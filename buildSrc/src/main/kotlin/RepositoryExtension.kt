import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.PasswordCredentials

fun RepositoryHandler.exampleGPR(action: (PasswordCredentials.() -> Unit)? = null) = maven {
    name = "GitHubPackages"
    setUrl("https://maven.pkg.github.com/Hott-J/auth-module-sample")
    credentials {
        if (action == null) {
            username = "be@example.ai"
            password = "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"
        } else {
            action()
        }
    }
}