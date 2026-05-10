import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun secretOrProperty(name: String): String =
    providers.environmentVariable(name).orElse(localProperties.getProperty(name, "")).get()

android {
    namespace = "com.batb4016.tinynext"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.batb4016.tinynext"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val releaseBannerId = secretOrProperty("ADMOB_BANNER_AD_UNIT_ID")
        val releaseAppId = secretOrProperty("ADMOB_APP_ID")
        buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$releaseBannerId\"")
        manifestPlaceholders["admobApplicationId"] =
            releaseAppId.ifBlank { "ca-app-pub-3940256099942544~3347511713" }
    }

    signingConfigs {
        create("playRelease") {
            val storeFilePath = secretOrProperty("TINYNEXT_RELEASE_STORE_FILE")
            if (storeFilePath.isNotBlank()) {
                storeFile = file(storeFilePath)
                storePassword = secretOrProperty("TINYNEXT_RELEASE_STORE_PASSWORD")
                keyAlias = secretOrProperty("TINYNEXT_RELEASE_KEY_ALIAS")
                keyPassword = secretOrProperty("TINYNEXT_RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("Boolean", "ALLOW_DEBUG_PREMIUM_OVERRIDE", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("Boolean", "ALLOW_DEBUG_PREMIUM_OVERRIDE", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val hasSigning = secretOrProperty("TINYNEXT_RELEASE_STORE_FILE").isNotBlank()
            if (hasSigning) {
                signingConfig = signingConfigs.getByName("playRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.6")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("com.google.android.gms:play-services-ads:25.2.0")
    implementation("com.android.billingclient:billing-ktx:8.3.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
