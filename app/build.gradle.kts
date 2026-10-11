plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.blokr"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.blokr.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            merges += "META-INF/io.netty.versions.properties"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":minosoft-core"))
}
