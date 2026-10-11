plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "de.bixilon.minosoft.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Netty is the transport framework used by Minosoft's original network client.
dependencies {
    // Original Minosoft packet buffers extend KUtil buffer implementations.
    implementation("de.bixilon:kutil:1.31")
    implementation("io.netty:netty-buffer:4.1.115.Final")
    implementation("io.netty:netty-transport:4.1.115.Final")
    implementation("io.netty:netty-codec:4.1.115.Final")
    testImplementation("junit:junit:4.13.2")
}
