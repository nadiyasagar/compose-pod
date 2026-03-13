plugins {
    alias(composepodLibs.plugins.android.library)
    alias(composepodLibs.plugins.kotlin.compose)
    id("maven-publish")
}

android {
    namespace = "com.brine.composepod"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        compose = true
    }
    
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(composepodLibs.androidx.core.ktx)
    implementation(composepodLibs.androidx.appcompat)
    implementation(composepodLibs.material)
    
    implementation(composepodLibs.androidx.lifecycle.runtime.ktx)
    implementation(platform(composepodLibs.androidx.compose.bom))
    implementation(composepodLibs.androidx.compose.ui)
    
    testImplementation(composepodLibs.junit)
    androidTestImplementation(composepodLibs.androidx.junit)
    androidTestImplementation(composepodLibs.androidx.espresso.core)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "com.github.nadiyasagar" // User needs to change this
            artifactId = "composepod"
            version = "1.0.3" // User needs to manage versions via GitHub releases too
        }
    }
}