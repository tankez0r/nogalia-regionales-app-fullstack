plugins {
    id("com.android.application")
}

android {
    namespace = "com.nogalina.regionales"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nogalina.regionales"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "API_URL", "\"https://mu-chilecito.playit.plus/nogalina/api/\"")
        buildConfigField("String", "CLIMA_URL", "\"https://api.open-meteo.com/\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.11.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.11.1")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
}
