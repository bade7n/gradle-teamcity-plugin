
pluginManagement {
    plugins {
        id ("com.github.rodm.teamcity-server") version "1.4"
    }
}

rootProject.name = "agent-tool-plugin"

includeBuild ("../..")

rootProject.buildFileName = "build.gradle.kts"
