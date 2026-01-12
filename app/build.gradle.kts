plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
    // kotlin("plugin.serialization") version "2.0.21"
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ola.fivethirtyeight"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ola.fivethirtyeight"
        minSdk = 26
        targetSdk = 36
        versionCode = 21
        versionName = "3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
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
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation (libs.compose)
    implementation(libs.androidx.material.icons.extended.android)


    //implementation("com.example.library:animatedplacement:1.0.0")
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation (libs.retrofit)

    implementation (libs.converter.scalars)
    implementation(libs.androidx.datastore.preferences)

    //Coil
    implementation(libs.coil.compose)
    implementation (libs.jsoup)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation (libs.androidx.constraintlayout.compose)

    implementation(libs.androidx.room.runtime)
   // annotationProcessor(libs.androidx.room.compiler)
    // To use Kotlin annotation processing tool (kapt)
   kapt(libs.androidx.room.compiler)
    // To use Kotlin Symbol Processing (KSP)
  //  ksp("androidx.room:room-compiler:$room_version")
  //implementation("com.google.dagger:hilt-android:2.44")
    // kapt("com.google.dagger:hilt-android-compiler:2.44")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    implementation (libs.compose)

    implementation(libs.accompanist.placeholder.material)

    implementation(libs.androidx.animation)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.accompanist.swiperefresh)
    //implementation(libs.androidx.material.pullrefresh)

   // implementation(libs.material3)

    // implementation(libs.androidx.material3.pullrefresh)

    implementation(libs.accompanist.webview)

    // Activity Compose
    //implementation(libs.androidx.activity.compose.v170)

    implementation(libs.accompanist.navigation.animation)

    //implementation(libs.accompanist.navigation.animation.v0301)
    implementation(libs.androidx.webkit)


    // Hilt + WorkManager integration
        implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)


// WorkManager
    implementation(libs.androidx.work.runtime.ktx)


    implementation(libs.hilt.android)


    implementation(libs.androidx.hilt.work)
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")

    implementation("com.rometools:rome:2.1.0")

    implementation("com.github.bumptech.glide:glide:5.0.5")
    kapt ("com.github.bumptech.glide:compiler:5.0.5")
    implementation ("com.github.bumptech.glide:compose:1.0.0-beta08")


    implementation("androidx.paging:paging-compose:3.3.6")

    implementation("androidx.room:room-runtime:2.8.4")
    //implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-paging:2.8.4") // ✅ REQUIRED
    //kapt("androidx.room:room-compiler:2.6.1")


}