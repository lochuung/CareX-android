plugins {
    alias(libs.plugins.android.application)
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
}

android {
    namespace = "hcmute.edu.vn.loclinhvabao.carex"
    compileSdk = 35

    defaultConfig {
        applicationId = "hcmute.edu.vn.loclinhvabao.carex"
        minSdk = 26
        targetSdk = 35
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

    // Enable Data Binding
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // Glide for image loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // ViewModel and LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.rxjava2)
    implementation(libs.room.rxjava3)
    implementation(libs.room.paging)

    // Google Play Services
    implementation(libs.play.services.fitness)
    implementation(libs.play.services.auth)
    
    // Firebase - sử dụng phiên bản BOM từ libs.versions.toml
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.storage)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)
    implementation(libs.google.firebase.database)
    implementation(libs.google.firebase.appcheck.playintegrity)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.video)
    implementation(libs.camerax.view)
    implementation(libs.camerax.extensions)

    // MPAndroidChart for charts and graphs
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

}