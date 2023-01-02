plugins {
    id("com.android.library")
}
group = "com.github.pedroSG94"

android {
    compileSdk = libs.versions.compile.sdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()
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
