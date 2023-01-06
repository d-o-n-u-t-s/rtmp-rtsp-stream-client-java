// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add the Crashlytics Gradle plugin.
        classpath(libs.com.google.firebase.crashlytics.gradle)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

//GradleDSLのissueでlibsにエラーが出るためSuppressをつける
//https://github.com/gradle/gradle/issues/22797
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    // Check that you have the Google Services Gradle plugin v4.3.2 or later
    // (if not, add it).
    alias(libs.plugins.com.google.gms.services) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.com.github.dcendents.android.maven) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
