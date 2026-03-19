plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "net.kibotu.compassview.compose"
    resourcePrefix = "compass_compose_"
    
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        disable += "InvalidPackage"
        abortOnError = false
        checkAllWarnings = false
        ignoreWarnings = true
        quiet = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = false
        verbose = true
    }
}

mavenPublishing {
    if (System.getenv("JITPACK") != "true") {
        publishToMavenCentral()
        signAllPublications()
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    
    // AndroidX
    implementation(libs.androidx.core.ktx)
}
