pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // Baixa o JDK 17 pedido pelo jvmToolchain(17) se a máquina não tiver.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Só para o Tesseract4Android (OCR), que não está no Maven Central.
        maven("https://jitpack.io") { content { includeGroup("cz.adaptech.tesseract4android") } }
    }
}

rootProject.name = "ProjetoFerramentas"
include(":app")
include(":opendesign")
