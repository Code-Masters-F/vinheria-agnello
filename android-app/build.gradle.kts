// [TokenOptimization] - Arquivo raiz de build; define versões compartilhadas via plugins block
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false
}
