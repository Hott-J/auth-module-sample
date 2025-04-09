plugins {
    `kotlin-dsl`
    id("com.google.cloud.tools.jib") version "3.4.3"
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.0")
}
