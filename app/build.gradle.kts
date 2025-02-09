plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {

    namespace = "com.tongteacrew.unihub"
    compileSdk = 35

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }

    defaultConfig {
        applicationId = "com.tongteacrew.unihub"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Our dependencies:
    implementation("androidx.core:core-splashscreen:1.0.1") // For splash screen
    implementation("com.github.bumptech.glide:glide:4.16.0") // For image library
    implementation("com.google.firebase:firebase-auth:23.1.0") // For authentication
    implementation("com.google.firebase:firebase-database:21.0.0") // For realtime database
    implementation("com.cloudinary:cloudinary-android:3.0.2") // For cloud storage
    implementation("com.google.firebase:firebase-messaging:24.1.0") // For notifications
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // For API calls
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0") // For Google Credentials

    // Default dependencies:
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}