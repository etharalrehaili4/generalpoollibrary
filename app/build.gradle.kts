@file:Suppress("DEPRECATION")

import io.gitlab.arturbosch.detekt.Detekt
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}
secrets {
    defaultPropertiesFileName = ".env"
}
android {
    namespace = "com.ntg.lmd"
    compileSdk = 36
    buildFeatures {
        buildConfig = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.ntg.lmd"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val mapsKey = (project.findProperty("MAPS_API_KEY") as String?) ?: System.getenv("MAPS_API_KEY") ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    lint {
        lintConfig = file("$rootDir/lint.xml")
    }
}

detekt {
    config = files("../detekt.yml")
    buildUponDefaultConfig = true

    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}
tasks.withType<Detekt>().configureEach {
    jvmTarget = "11" // match your compileOptions/kotlinOptions
}

// Custom detekt task for unused imports only
tasks.register("detektUnusedImports", io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Run detekt analysis for unused imports only"
    group = "verification"

    config.setFrom(files("../detekt-unused-imports.yml"))

    reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
    }
}

// Custom detekt task for file size and method size constraints
tasks.register("detektFileSize", io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Run detekt analysis for file size and method size constraints"
    group = "verification"

    config.setFrom(files("../detekt-file-size.yml"))

    source = fileTree("src/main/java")

    reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Retrofit
    implementation(libs.retrofit)
    // Gson Converter
    implementation(libs.converter.gson)
    // OkHttp
    implementation(libs.okhttp)
    // OkHttp Logging Interceptor
    implementation(libs.logging.interceptor)
    // Lifecycle ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // Icons
    implementation(libs.androidx.material.icons.extended)
    // animation
    implementation(libs.androidx.animation)

    // Icons
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material)

    // Paging 3
    implementation(libs.paging.runtime.ktx)
    implementation(libs.paging.compose)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    // security
    implementation(libs.androidx.security.crypto)

    implementation(libs.material.icons.extended)

    // LeakCanary for memory leak detection
    debugImplementation(libs.leakcanary.android)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation("com.github.Raghad-almehmadi:network_library:3")

    // vertical list library
    implementation("com.github.etharalrehaili4:verticallist:834045a12a")

    //location lib
    implementation("com.github.R-0515:core-location:-SNAPSHOT")
}
// Custom tasks for code quality checks
tasks.register("checkUnusedAssets") {
    group = "verification"
    description = "Check for unused assets in the project"

    doLast {
        val resDir = file("src/main/res")
        val drawableDir = file("src/main/res/drawable")
        val mipmapDir = file("src/main/res/mipmap-anydpi-v26")
        val valuesDir = file("src/main/res/values")

        val unusedAssets = mutableListOf<String>()

        // Check drawable resources
        if (drawableDir.exists()) {
            drawableDir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension in listOf("xml", "png", "jpg", "jpeg", "webp")) {
                    val resourceName = file.nameWithoutExtension
                    if (!isResourceUsed(resourceName, "drawable")) {
                        unusedAssets.add("drawable/${file.name}")
                    }
                }
            }
        }

        // Check mipmap resources
        if (mipmapDir.exists()) {
            mipmapDir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "xml") {
                    val resourceName = file.nameWithoutExtension
                    if (!isResourceUsed(resourceName, "mipmap")) {
                        unusedAssets.add("mipmap/${file.name}")
                    }
                }
            }
        }

        if (unusedAssets.isNotEmpty()) {
            println("❌ Found ${unusedAssets.size} unused assets:")
            unusedAssets.forEach { println("   - $it") }
            println("⚠️  Note: Launcher-related drawables (ic_launcher_*) are often unused if using bitmap launcher icons.")
            throw GradleException("Unused assets found. Please remove them or use them in your code.")
        } else {
            println("✅ No unused assets found")
        }
    }
}

tasks.register("checkUnusedFiles") {
    group = "verification"
    description = "Check for unused Kotlin/Java files in the project"

    doLast {
        val srcDir = file("src/main/java")
        val unusedFiles = mutableListOf<String>()

        if (srcDir.exists()) {
            findUnusedFiles(srcDir, unusedFiles)
        }

        if (unusedFiles.isNotEmpty()) {
            println("❌ Found ${unusedFiles.size} potentially unused files:")
            unusedFiles.forEach { println("   - $it") }
            println("⚠️  Note: This is a basic check. Some files might be used dynamically.")
            throw GradleException("Potentially unused files found. Please review and remove if not needed.")
        } else {
            println("✅ No unused files found")
        }
    }
}

// Helper function to check if a resource is used
fun isResourceUsed(
    resourceName: String,
    resourceType: String,
): Boolean {
    val srcDir = file("src/main/java")
    val layoutDir = file("src/main/res/layout")
    val valuesDir = file("src/main/res/values")
    val manifestFile = file("src/main/AndroidManifest.xml")

    // Check in Kotlin/Java files
    if (srcDir.exists()) {
        val used = findResourceUsageInFiles(srcDir, resourceName, resourceType)
        if (used) return true
    }

    // Check in layout files
    if (layoutDir.exists()) {
        val used = findResourceUsageInFiles(layoutDir, resourceName, resourceType)
        if (used) return true
    }

    // Check in values files
    if (valuesDir.exists()) {
        val used = findResourceUsageInFiles(valuesDir, resourceName, resourceType)
        if (used) return true
    }

    // Check in AndroidManifest.xml
    if (manifestFile.exists()) {
        val used = findResourceUsageInFiles(manifestFile.parentFile, resourceName, resourceType)
        if (used) return true
    }

    return false
}

// Helper function to find resource usage in files
fun findResourceUsageInFiles(
    dir: File,
    resourceName: String,
    resourceType: String,
): Boolean {
    dir.walkTopDown().forEach { file ->
        if (file.isFile && file.extension in listOf("kt", "java", "xml")) {
            val content = file.readText()
            val patterns =
                listOf(
                    "@$resourceType/$resourceName",
                    "R.$resourceType.$resourceName",
                    "\"$resourceName\"",
                )

            if (patterns.any { pattern -> content.contains(pattern) }) {
                return true
            }
        }
    }
    return false
}

// Helper function to find unused files
fun findUnusedFiles(
    dir: File,
    unusedFiles: MutableList<String>,
) {
    dir.walkTopDown().forEach { file ->
        if (file.isFile && file.extension in listOf("kt", "java")) {
            val className = file.nameWithoutExtension
            val packagePath =
                file.parentFile
                    .relativeTo(dir)
                    .path
                    .replace("/", ".")
            val fullClassName = if (packagePath.isNotEmpty()) "$packagePath.$className" else className

            if (!isFileUsed(file, fullClassName)) {
                unusedFiles.add(file.relativeTo(project.projectDir).path)
            }
        }
    }
}

// Helper function to check if a file is used
fun isFileUsed(
    file: File,
    fullClassName: String,
): Boolean {
    val srcDir = file("src/main/java")

    // Skip certain files that are typically used
    val skipPatterns =
        listOf(
            "MainActivity",
            "MyApp",
            "Application",
            "Activity",
            "Fragment",
            "ViewModel",
            "Repository",
            "UseCase",
            "Service",
            "Receiver",
            "Provider",
            "Test",
            "Example",
            "Screen",
            "Theme",
            "Color",
            "Type",
            "Font",
            "NavGraph",
            "Module",
            "DataSource",
            "ApiClient",
            "Request",
            "Response",
            "UiState",
            "ApiResult",
            "Validator",
        )

    if (skipPatterns.any { pattern -> fullClassName.contains(pattern) }) {
        return true
    }

    // Check if the class is imported or referenced in other files
    srcDir.walkTopDown().forEach { otherFile ->
        if (otherFile != file && otherFile.isFile && otherFile.extension in listOf("kt", "java")) {
            val content = otherFile.readText()
            val className = file.nameWithoutExtension

            // Check for various reference patterns
            val referencePatterns =
                listOf(
                    fullClassName,
                    className,
                    "import.*$fullClassName",
                    "import.*$className",
                    "@Composable.*$className",
                    "class.*$className",
                    "object.*$className",
                    "interface.*$className",
                )

            if (referencePatterns.any { pattern -> content.contains(pattern) }) {
                return true
            }
        }
    }

    return false
}
