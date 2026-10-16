// Role: Module build configuration defining Android plugins, SDK settings, dependencies, and compiler options.
// Purpose: Controls the module build environment and dependencies; it does not contain screen business logic.
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
}

val apiProperties = Properties().apply {
    val file = rootProject.file("api.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun escapeBuildConfigValue(value: String): String = value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

// History owns a separate Gemini configuration. It is not shared with the other modules.
val historyGeminiApiKey = apiProperties.getProperty("HISTORY_GEMINI_API_KEY")
    ?: ""
val historyGeminiModel = apiProperties.getProperty("HISTORY_GEMINI_MODEL")
    ?: "gemini-flash-latest"

android {
    namespace = "com.example.historymodule"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField(
            "String",
            "HISTORY_GEMINI_API_KEY",
            "\"${escapeBuildConfigValue(historyGeminiApiKey)}\""
        )
        buildConfigField(
            "String",
            "HISTORY_GEMINI_MODEL",
            "\"${escapeBuildConfigValue(historyGeminiModel)}\""
        )
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity)
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
