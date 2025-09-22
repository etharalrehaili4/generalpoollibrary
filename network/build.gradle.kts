import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ntg.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        val envFile = rootProject.file(".env")
        val props =
            Properties().apply {
                if (envFile.exists()) {
                    envFile.inputStream().use { this.load(it) }
                }
            }
        val baseUrl = props.getProperty("BASE_URL") ?: error("Missing BASE_URL in .env")
        val wsBaseUrl = props.getProperty("WS_BASE_URL") ?: error("Missing WS_BASE_URL in .env")
        val supaKey = props.getProperty("SUPABASE_KEY") ?: error("Missing SUPABASE_KEY in .env")

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "WS_BASE_URL", "\"$wsBaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supaKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    buildFeatures { buildConfig = true }
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
    api(libs.okhttp.v4120)
    implementation(libs.logging.interceptor.v4120)
    api(libs.retrofit.v2110)
    implementation(libs.converter.gson.v2110)
    implementation(libs.kotlinx.coroutines.android.v190)
    implementation(libs.androidx.security.crypto.v110alpha06)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}