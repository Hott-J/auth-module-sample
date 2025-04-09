import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

fun PublishingExtension.uploadToGPR(project: Project, artifactId: String) {
    repositories {
        exampleGPR {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            this.from(project.components["java"])
            this.artifactId = artifactId
            this.groupId = project.group.toString()
            this.version = project.version.toString()
        }
    }
}