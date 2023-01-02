plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val COMPILE_SDK: String by project
val MIN_SDK_VERSION: String by project
val TARGET_SDK_VERSION: String by project
val KOTLIN_VERSION: String by project
android {
    compileSdk = COMPILE_SDK.toInt()
    
    defaultConfig {
        applicationId = "com.pedro.rtpstreamer"
        minSdk = MIN_SDK_VERSION.toInt()
        targetSdk = TARGET_SDK_VERSION.toInt()
        versionCode = 199
        versionName = "1.9.9"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KOTLIN_VERSION")
}
