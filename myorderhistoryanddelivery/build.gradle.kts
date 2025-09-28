plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myorderhistoryanddelivery"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        compose = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core + appcompat + material (XML world)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose — use the BOM via platform(...)
    implementation(platform(libs.androidx.compose.bom.v20250901))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("com.github.Raghad-almehmadi:network_library:3")

    // vertical list library
    implementation("com.github.etharalrehaili4:verticallist:834045a12a")
    // Activity Compose (your pinned alias)
    implementation(libs.androidx.activity.compose.v192)
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    // Gson for JSON serialization/deserialization
    implementation(libs.gson)

    // Retrofit with Gson converter (if you’re using Retrofit)
    implementation(libs.converter.gson.v2110)

    // Icons
    implementation(libs.androidx.material.icons.extended)

    implementation("com.github.R-0515:core-location:-SNAPSHOT")

    // Navigation
    implementation(libs.androidx.navigation.compose)

}