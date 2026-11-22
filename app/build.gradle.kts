import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Versão: vem da tag do Git no CI (-PversaoNome=1.2.3); localmente usa a padrão.
// versionCode = maior * 10000 + menor * 100 + correção, sempre crescente para o Obtainium.
val versaoNome = (findProperty("versaoNome") as String?)?.removePrefix("v") ?: "0.1.0"
val versaoCodigo = versaoNome.split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
    .let { (it.getOrElse(0) { 0 } * 10000) + (it.getOrElse(1) { 0 } * 100) + it.getOrElse(2) { 0 } }

// Assinatura da release: keystore.properties local ou variáveis de ambiente no CI.
val assinatura = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun segredo(chave: String, ambiente: String): String? = assinatura.getProperty(chave) ?: System.getenv(ambiente)

android {
    namespace = "io.github.andrexagris.ferramentas"
    compileSdk {
        // 37.2: o Compose 1.13 pede a API 37.1 ou mais nova para compilar (não muda o targetSdk).
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "io.github.andrexagris.ferramentas"
        minSdk = 33 // Android 13, o último do Galaxy S20 FE
        targetSdk = 37
        versionCode = versaoCodigo
        versionName = versaoNome
    }

    signingConfigs {
        create("release") {
            val arquivo = segredo("storeFile", "FT_KEYSTORE_ARQUIVO")
            if (arquivo != null) {
                storeFile = rootProject.file(arquivo)
                storePassword = segredo("storePassword", "FT_KEYSTORE_SENHA")
                keyAlias = segredo("keyAlias", "FT_CHAVE_ALIAS")
                keyPassword = segredo("keyPassword", "FT_CHAVE_SENHA")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            ndk { abiFilters += listOf("arm64-v8a", "x86_64") } // x86_64 para o emulador
        }
        release {
            // Sem R8: várias bibliotecas (dnsjava, Tesseract) usam reflexão e JNI, e o app é aberto (GPL).
            isMinifyEnabled = false
            ndk { abiFilters += listOf("arm64-v8a") } // S20 FE, A24 e todo celular atual são arm64
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isReturnDefaultValues = true }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging { resources.excludes += listOf("META-INF/*.md", "META-INF/LICENSE*", "META-INF/NOTICE*") }
}

kotlin { jvmToolchain(17) }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(project(":opendesign"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.zxing.core)
    implementation(libs.dnsjava)
    implementation(libs.tesseract)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
