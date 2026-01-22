plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.smartcloset_frontend"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartcloset_frontend"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // ローカルプロパティからサーバーURLを取得してBuildConfigに設定
        val serverUrl = project.rootProject.file("local.properties").readLines()
            .find { it.startsWith("SERVER_URL") }
            ?.split("=")
            ?.get(1)
            ?.trim()
        buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.foundation.android)
//    implementation(libs.androidx.ui.graphics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //compose
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.3")
    implementation("androidx.compose.material:material-icons-extended:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Retrofit & Kotlinx Serialization
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Compose 用 Activity
    implementation("androidx.activity:activity-compose:1.9.2")
    
    // Material Icons Extended (メール・ロックアイコン用)
    implementation("androidx.compose.material:material-icons-extended:1.7.3")
    //GPS取得用
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // QRコード生成用
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    // DataStore
        implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // WorkManager (バックグラウンド処理用)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // EXIF情報読み取り用
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}