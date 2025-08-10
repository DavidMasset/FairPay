plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "es.ifp.fairpay"
    compileSdk = 35

    defaultConfig {
        applicationId = "es.ifp.fairpay"
        minSdk = 28
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Dependencias de Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Web3j para integración con Ethereum
    implementation("org.web3j:core:4.9.8")
    
    // Para manejo de claves privadas
    implementation("org.bitcoinj:bitcoinj-core:0.16.2")
    
    // Para peticiones HTTP
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}