plugins {
    id("com.android.library")
}
group = "com.github.pedroSG94"

val COMPILE_SDK: String by project
val MIN_SDK_VERSION: String by project
val TARGET_SDK_VERSION: String by project

android {
    compileSdk = COMPILE_SDK.toInt()
    
    defaultConfig {
        minSdk = MIN_SDK_VERSION.toInt()
        targetSdk = TARGET_SDK_VERSION.toInt()
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
dependencies {
    api("androidx.annotation:annotation:1.1.0")
}
