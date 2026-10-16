// Role: Module build configuration defining Android plugins, SDK settings, dependencies, and compiler options.
// Purpose: Controls the module build environment and dependencies; it does not contain screen business logic.
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.asm_final"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.asm_final"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":data-core"))
    implementation(project(":ui-core"))
    implementation(project(":math"))
    implementation(project(":science"))
    implementation(project(":programming"))
    implementation(project(":history"))
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
