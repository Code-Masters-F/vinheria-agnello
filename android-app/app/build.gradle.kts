// [TokenOptimization] - App-level build.gradle.kts com todas as dependências do módulo
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "br.com.agnellovinheria.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.com.agnellovinheria.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)

    // Android Instrumented Tests
    androidTestImplementation(libs.test.runner)
}

// [TokenOptimization] - Exporta o schema do Room via argumento KSP
// (sem precisar do plugin adicional androidx.room)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
