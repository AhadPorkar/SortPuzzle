plugins {
    alias(libs.plugins.android.application)

    // Removed: AGP 9 provides built-in Kotlin support.
    // alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.parsgames.sortpuzzle"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.parsgames.sortpuzzle"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        // فقط فارسی و انگلیسی در بسته نهایی نگه داشته می‌شود.
        resourceConfigurations += setOf("fa", "en")

        // شناسه‌های تست AdMob
        buildConfigField(
            "String",
            "ADMOB_APP_ID",
            "\"ca-app-pub-3940256099942544~3347511713\""
        )
        buildConfigField(
            "String",
            "AD_UNIT_BANNER",
            "\"ca-app-pub-3940256099942544/6300978111\""
        )
        buildConfigField(
            "String",
            "AD_UNIT_INTERSTITIAL",
            "\"ca-app-pub-3940256099942544/1033173712\""
        )
        buildConfigField(
            "String",
            "AD_UNIT_REWARDED",
            "\"ca-app-pub-3940256099942544/5224354917\""
        )
        buildConfigField(
            "String",
            "AD_UNIT_APP_OPEN",
            "\"ca-app-pub-3940256099942544/9257395921\""
        )

        manifestPlaceholders["admobAppId"] =
            "ca-app-pub-3940256099942544~3347511713"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = false
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}"
        )
    }
}

kotlin {
    compilerOptions {
        // jvmTarget is automatically derived from targetCompatibility
        // when using AGP built-in Kotlin.
        optIn.add("kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.animation)

    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.play.billing.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.play.review.ktx)

    testImplementation(libs.junit)
}