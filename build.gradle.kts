// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.lombok)
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.0")
    }
}