plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = libs.versions.compile.sdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()
        applicationId = properties["APPLICATION_ID"] as String
        versionCode = libs.versions.rtmp.version.code.get().toInt()
        versionName = libs.versions.rtmp.version.name.get()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":rtplibrary"))
    implementation(libs.com.google.firebase.crashlytics)
    implementation(libs.com.google.firebase.analytics)
    implementation(libs.com.google.android.material)
}
