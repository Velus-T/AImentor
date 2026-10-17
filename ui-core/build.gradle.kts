plugins { alias(libs.plugins.android.library) }
android {
    namespace = "com.example.mentor.ui"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":data-core"))
    implementation("androidx.recyclerview:recyclerview:1.4.0")
}
