// O Kotlin vem embutido no Android Gradle Plugin 9 (built-in Kotlin): não se aplica mais
// org.jetbrains.kotlin.android. O plugin do compilador do Compose continua separado.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
