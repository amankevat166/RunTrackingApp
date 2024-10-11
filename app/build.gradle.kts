plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt.plugin)
//    id("kotlin-kapt")
    kotlin("kapt")

}


android {
    namespace = "com.example.runningapp2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.runningapp2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    buildFeatures{
        dataBinding = true
        viewBinding = true
    }

}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //hilt - dagger
    implementation(libs.dagger.hilt)
    kapt(libs.dagger.hilt.compiler)

    //room
    implementation(libs.androidx.room.runtime)
    kapt (libs.androidx.room.compiler)

    //Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    //coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    //coroutine lifecycle scope
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)

    //Navigation Components
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation (libs.androidx.navigation.compose)

    //Glide
    implementation (libs.github.glide)
    kapt (libs.compiler)

    //google map location services
    implementation (libs.play.services.location)
    implementation (libs.play.services.maps)

    // Easy Permissions
    implementation (libs.easypermissions)

    // Timber
    implementation (libs.timber)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

   // implementation (libs.chart.extensions)



}
