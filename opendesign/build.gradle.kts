// Módulo de biblioteca do Open Design para Jetpack Compose.
// No projeto do app: copie a pasta opendesign/ para a raiz, acrescente include(":opendesign") no
// settings.gradle.kts e implementation(project(":opendesign")) nas dependências do app.
// As versões dos plugins vêm do projeto do app (plugins { … apply false } na raiz).
// [Projeto Ferramentas] Sem org.jetbrains.kotlin.android: o AGP 9 já traz o Kotlin embutido.
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.andrexagris.opendesign"
    compileSdk { version = release(37) { minorApiLevel = 2 } } // [Projeto Ferramentas] era 36

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin { jvmToolchain(17) }

dependencies {
    // Mínimo: Compose 1.9 (BOM 2025.08.00), que trouxe Modifier.dropShadow e innerShadow, usados no relevo.
    // Se o app já usa um BOM mais novo, o Gradle fica com o mais novo.
    val bom = platform("androidx.compose:compose-bom:2025.08.00")
    api(bom)
    api("androidx.compose.ui:ui")
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
}
