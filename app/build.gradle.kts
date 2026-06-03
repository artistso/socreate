import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

// ─── Signing Configuration ──────────────────────────────────────────────
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.socreate.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.socreate.app"
        minSdk = 34
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"

        buildConfigField("String", "DEVELOPER_NAME", "\"Steven Michael Allen Owens\"")
        buildConfigField("String", "DEVELOPER_HANDLE", "\"@SoQuarky\"")
        buildConfigField("String", "COMPANY_NAME", "\"AdventuresInDrawing\"")
        buildConfigField("String", "PRIMARY_BRAND", "\"Soquarky\"")
        buildConfigField("String", "SUPPORT_EMAIL", "\"support@soquarky.click\"")
        buildConfigField("String", "WEBSITE", "\"https://soquarky.click\"")
        buildConfigField("String", "CRASH_REPORT_EMAIL", "\"soquarky@artistso.com\"")
        buildConfigField("String", "ARTISTSO_URL", "\"https://artistso.com\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Use splits.abi for ABI filtering instead of ndk.abiFilters
        // (having both causes "Conflicting configuration" error)
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.containsKey("storeFile")) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("Boolean", "ENABLE_GPU_DEBUG", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "ENABLE_GPU_DEBUG", "false")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Native build disabled until GPU renderer is implemented
    // externalNativeBuild {
    //     cmake {
    //         path = file("src/main/cpp/CMakeLists.txt")
    //         version = "3.22.1"
    //     }
    // }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
            isUniversalApk = false
        }
    }

    // Temporarily exclude native build until C++ code is ready
    // This prevents build failures from the CMake stub
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")

    // Lifecycle & MVI
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Hilt — DI framework
    implementation("com.google.dagger:hilt-android:2.49")
    ksp("com.google.dagger:hilt-compiler:2.49")

    // Stylus & Rendering for Tab S10+
    implementation("androidx.graphics:graphics-core:1.0.0")
    implementation("androidx.input:input-motionprediction:1.0.0-beta01")

    // Image loading
    implementation("io.coil-kt:coil:2.5.0")

    // Google Sign-In (user ↔ Google only; developer never sees credentials)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // OkHttp for network requests (artistso.com API, GitHub crash reports)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // WebView for artistso.com content
    implementation("androidx.webkit:webkit:1.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
