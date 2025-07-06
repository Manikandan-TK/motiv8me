// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.hilt) apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}