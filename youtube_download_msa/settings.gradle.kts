pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "youtube_download_msa"

include("contracts", "gateway", "download-api", "media-worker", "search-api")
