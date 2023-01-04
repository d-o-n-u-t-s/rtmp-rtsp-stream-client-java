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
    implementation("com.google.firebase:firebase-crashlytics:17.3.0")
    implementation("com.google.firebase:firebase-analytics:18.0.1")
    implementation("com.google.android.material:material:1.3.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${libs.versions.kotlin.get()}")
}
