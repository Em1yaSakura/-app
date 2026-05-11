pluginManagement {
    repositories {
        // 部分网络环境下，直连 Google Maven 可能出现 TLS 握手失败。
        // 这里优先使用镜像仓库（如阿里云），必要时再回退到官方仓库。
        maven(url = "https://maven.aliyun.com/repository/google") {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven(url = "https://maven.aliyun.com/repository/central")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/central")
        // 作为兜底（如果你的网络允许直连）
        google()
        mavenCentral()
    }
}

rootProject.name = "EnglishAPP"
include(":app")
