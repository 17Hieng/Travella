plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.csian.travella"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.csian.travella"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}



secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.")       // Ignore all keys matching the regexp "sdk."
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.android.gms:play-services-auth:21.2.0")  // Check for the latest version
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.github.AnyChart:AnyChart-Android:1.1.2") // this line is for pie chart
    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation ("androidx.core:core:1.6.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.0")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("androidx.webkit:webkit:1.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.google.maps.android:android-maps-utils:2.2.5")
    implementation(libs.play.services.maps)
    implementation(libs.places)
    implementation(libs.androidx.room.ktx)
    implementation(libs.protolite.well.known.types)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.github.bumptech.glide:glide:4.16.0" )
}

apply(plugin = "com.google.gms.google-services")