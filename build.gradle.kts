
plugins {
    id ("org.gradle.groovy")
    id ("org.gradle.java-gradle-plugin")
    id ("org.gradle.jacoco")
    id ("org.gradle.maven-publish")
    id ("com.gradle.plugin-publish") version "0.15.0"
    id ("org.jetbrains.kotlin.jvm") version "1.4.21"
    id ("org.sonarqube") version "3.3"
}

version = "1.5-SNAPSHOT"
group = "io.github.rodm"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("de.undercouch:gradle-download-task:5.1.0")

    implementation (platform("com.github.docker-java:docker-java-bom:3.2.13"))
    implementation ("com.github.docker-java:docker-java-core")
    implementation ("com.github.docker-java:docker-java-transport-httpclient5")

    compileOnly ("org.jetbrains.intellij.plugins:structure-base:3.190")
    compileOnly ("org.jetbrains.intellij.plugins:structure-teamcity:3.190")
    compileOnly ("org.jetbrains.intellij:plugin-repository-rest-client:2.0.20")
    compileOnly ("org.jetbrains:marketplace-zip-signer:0.1.8")

    testImplementation(platform("org.junit:junit-bom:5.8.2")) {
        because("Platform, Jupiter, and Vintage versions should match")
    }
    testImplementation ("org.junit.jupiter:junit-jupiter")
    testImplementation ("org.xmlunit:xmlunit-matchers:2.9.0")
    testImplementation ("org.hamcrest:hamcrest-library:2.2")
    testImplementation ("org.mockito:mockito-core:4.6.1")

    testImplementation ("org.jetbrains:marketplace-zip-signer:0.1.8")
    testImplementation ("org.jetbrains.intellij.plugins:structure-base:3.190")
    testImplementation ("org.jetbrains.intellij.plugins:structure-teamcity:3.190")
    testImplementation ("org.jetbrains.intellij:plugin-repository-rest-client:2.0.20")
}

sourceSets {
    register("functional") {
        compileClasspath += main.get().output + configurations.testRuntimeClasspath
        runtimeClasspath += main.get().output + configurations.testRuntimeClasspath
    }
    register("samples") {
        compileClasspath += main.get().output + configurations.testRuntimeClasspath
        runtimeClasspath += main.get().output + configurations.testRuntimeClasspath
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

jacoco {
    toolVersion = "0.8.7"
}

gradlePlugin {
    testSourceSets (sourceSets["functional"], sourceSets["samples"])

    plugins {
        create("teamcityBasePlugin") {
            id = "io.github.rodm.teamcity-base"
            displayName = "Gradle TeamCity base plugin"
            implementationClass = "com.github.rodm.teamcity.TeamCityBasePlugin"
        }
        create("teamcityServerPlugin") {
            id = "io.github.rodm.teamcity-server"
            displayName = "Gradle TeamCity Server plugin"
            implementationClass = "com.github.rodm.teamcity.TeamCityServerPlugin"
        }
        create("teamcityAgentPlugin") {
            id = "io.github.rodm.teamcity-agent"
            displayName = "Gradle TeamCity Agent plugin"
            implementationClass = "com.github.rodm.teamcity.TeamCityAgentPlugin"
        }
        create("teamcityCommonPlugin") {
            id = "io.github.rodm.teamcity-common"
            displayName = "Gradle TeamCity Common API plugin"
            implementationClass = "com.github.rodm.teamcity.TeamCityCommonPlugin"
        }
        create("teamcityEnvironmentsPlugin") {
            id = "io.github.rodm.teamcity-environments"
            displayName = "Gradle TeamCity Environments plugin"
            implementationClass = "com.github.rodm.teamcity.TeamCityEnvironmentsPlugin"
        }

        create("deprecatedServerPlugin") {
            id = "com.github.rodm.teamcity-server"
            displayName = "Gradle TeamCity Server plugin"
            implementationClass = "com.github.rodm.teamcity.DeprecatedTeamCityServerPlugin"
        }
        create("deprecatedAgentPlugin") {
            id = "com.github.rodm.teamcity-agent"
            displayName = "Gradle TeamCity Agent plugin"
            implementationClass = "com.github.rodm.teamcity.DeprecatedTeamCityAgentPlugin"
        }
        create("deprecatedCommonPlugin") {
            id = "com.github.rodm.teamcity-common"
            displayName = "Gradle TeamCity Common API plugin"
            implementationClass = "com.github.rodm.teamcity.DeprecatedTeamCityCommonPlugin"
        }
        create("deprecatedEnvironmentsPlugin") {
            id = "com.github.rodm.teamcity-environments"
            displayName = "Gradle TeamCity Environments plugin"
            implementationClass = "com.github.rodm.teamcity.DeprecatedTeamCityEnvironmentsPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            from (components["java"])
        }
    }
}

pluginBundle {
    website = "https://github.com/rodm/gradle-teamcity-plugin"
    vcsUrl = "https://github.com/rodm/gradle-teamcity-plugin"
    description = "Gradle plugin for developing TeamCity plugins"
    tags = listOf("teamcity")
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy (jacocoTestReport)
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }

    register<Test>("functionalTest") {
        description = "Runs the functional tests."
        group = "verification"
        useJUnitPlatform()
        testClassesDirs = sourceSets["functional"].output.classesDirs
        classpath = sourceSets["functional"].runtimeClasspath
    }

    register<Test>("samplesTest") {
        description = "Runs the sample builds."
        group = "verification"
        useJUnitPlatform()
        testClassesDirs = sourceSets["samples"].output.classesDirs
        classpath = sourceSets["samples"].runtimeClasspath
    }
}
