import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.compose)
            implementation(project(":domain"))
            implementation(project(":shared"))
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.core:core-ktx:1.15.0")
            implementation(libs.koin.android)
            implementation(libs.android.pdf.viewer)
            implementation(libs.androidx.browser)
            implementation(libs.firebase.messaging.native)
            implementation("com.google.android.gms:play-services-ads:23.6.0")
            // Coil network image loading needs an HTTP engine; OkHttp bundles one for Android.
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
            implementation(project(":data"))
        }
        iosMain.dependencies {
            implementation(libs.coil.network.ktor)
            implementation("io.ktor:ktor-client-darwin:3.0.1")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.studies.rrbmustudies.resources"
    generateResClass = always
}

// Google-provided test IDs used when a real value is absent from local.properties.
val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testBannerId = "ca-app-pub-3940256099942544/6300978111"
val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val testRewardedId = "ca-app-pub-3940256099942544/5224354917"
val testRewardedInterstitialId = "ca-app-pub-3940256099942544/5354046379"
val testNativeId = "ca-app-pub-3940256099942544/2247696110"

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun adProp(key: String, fallback: String): String =
    (localProps.getProperty(key) ?: "").ifBlank { fallback }

android {
    namespace = "com.studies.rrbmustudies"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.studies.rrbmustudies"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        // Debug always uses Google test IDs. Release overrides below.
        manifestPlaceholders["admobAppId"] = testAdmobAppId
        buildConfigField("String", "ADMOB_BANNER", "\"$testBannerId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL", "\"$testInterstitialId\"")
        buildConfigField("String", "ADMOB_REWARDED", "\"$testRewardedId\"")
        buildConfigField("String", "ADMOB_REWARDED_INTERSTITIAL", "\"$testRewardedInterstitialId\"")
        buildConfigField("String", "ADMOB_NATIVE", "\"$testNativeId\"")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            // Real IDs pulled from local.properties (never committed); fall back to test IDs.
            manifestPlaceholders["admobAppId"] = adProp("admob.app.id", testAdmobAppId)
            buildConfigField(
                "String", "ADMOB_BANNER",
                "\"${adProp("admob.banner.default", testBannerId)}\"",
            )
            buildConfigField(
                "String", "ADMOB_INTERSTITIAL",
                "\"${adProp("admob.interstitial.paper", testInterstitialId)}\"",
            )
            buildConfigField(
                "String", "ADMOB_REWARDED",
                "\"${adProp("admob.rewarded.offline", testRewardedId)}\"",
            )
            buildConfigField(
                "String", "ADMOB_REWARDED_INTERSTITIAL",
                "\"${adProp("admob.rewarded.interstitial", testRewardedInterstitialId)}\"",
            )
            buildConfigField(
                "String", "ADMOB_NATIVE",
                "\"${adProp("admob.native.default", testNativeId)}\"",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
