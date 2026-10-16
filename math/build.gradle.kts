import java.util.Properties

// Role: Module build configuration defining Android plugins, SDK settings, dependencies, and compiler options.
// Purpose: Controls the module build environment and dependencies; it does not contain screen business logic.
plugins {
    alias(libs.plugins.android.library)
}

val mathApiProperties = Properties().apply {
    val file = rootProject.file("api.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun mathSetting(name: String, fallback: String = ""): String =
    mathApiProperties.getProperty(name) ?: fallback
fun mathQuoted(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.mathmodule"
    buildFeatures { buildConfig = true }
    compileSdk = 36

    defaultConfig {
        buildConfigField("String", "MATH_GEMINI_API_KEY_1", mathQuoted(mathSetting("MATH_GEMINI_API_KEY_1")))
        buildConfigField("String", "MATH_GEMINI_API_KEY_2", mathQuoted(mathSetting("MATH_GEMINI_API_KEY_2", "")))
        buildConfigField("String", "MATH_GEMINI_API_KEY_3", mathQuoted(mathSetting("MATH_GEMINI_API_KEY_3", "")))
        buildConfigField("String", "MATH_GEMINI_MODEL", mathQuoted(mathSetting("MATH_GEMINI_MODEL", "gemini-flash-latest")))
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
