gradle.beforeProject {
    extra["kotlin.version"] = "2.4.21"
}

rootProject.name = "multi_tenant"
include("domain", "application", "adapter-web", "adapter-persistence", "bootstrap")
