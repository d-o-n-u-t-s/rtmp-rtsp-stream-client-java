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
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    api(project(":encoder"))
    api(project(":rtmp"))
    api(project(":rtsp"))
}
