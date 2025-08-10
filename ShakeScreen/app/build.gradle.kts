@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mnazar.shakescreen"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mnazar.shakescreen"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Optimize for specific architectures (removes unused native libraries)
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    buildTypes {
        release {
            // Enable code shrinking and obfuscation
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Optimize APK size
            packaging {
                resources {
                    excludes += listOf(
                        "/META-INF/{AL2.0,LGPL2.1}",
                        "/META-INF/DEPENDENCIES",
                        "/META-INF/LICENSE",
                        "/META-INF/LICENSE.txt",
                        "/META-INF/license.txt",
                        "/META-INF/NOTICE",
                        "/META-INF/NOTICE.txt",
                        "/META-INF/notice.txt",
                        "/META-INF/ASL2.0",
                        "/META-INF/*.kotlin_module"
                    )
                }
            }
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
        buildConfig = true
    }

    // Lint configuration for release optimization
    lint {
        checkReleaseBuilds = true
        abortOnError = false
        warningsAsErrors = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }

    // Bundle configuration for Play Store
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
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
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.materialIconsExtended)
    implementation(libs.materialIcons)
}