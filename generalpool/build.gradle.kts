plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.generalpool"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)

    // location lib
    implementation("com.github.R-0515:core-location:-SNAPSHOT")
    // Retrofit
    implementation(libs.retrofit)
    // Gson Converter
    implementation(libs.converter.gson)
    // OkHttp
    implementation(libs.okhttp)
    // network
    implementation("com.github.Raghad-almehmadi:network_library:3")
    // horizontal
    implementation("com.github.shsaudhrb:HorizontalList:0882e3c3a0")
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation("com.github.shsaudhrb:HorizontalList:0882e3c3a0")
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // security
    implementation(libs.androidx.security.crypto)
    // vertical list library
    implementation("com.github.etharalrehaili4:verticallist:834045a12a")


}