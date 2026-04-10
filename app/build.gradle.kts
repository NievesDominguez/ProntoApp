import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.2"
    alias(libs.plugins.google.gms.google.services)
}

// Leer la API key desde local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}

val groqApiKey: String = localProperties.getProperty("GROQ_API_KEY") ?: ""

android {
    namespace = "com.example.persistencia"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.persistencia"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Añadir la API key al BuildConfig
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")

        /*ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }*/
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Reproducir vídeos de youtube
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    // Dependencia local para el SDK de Redsys
    implementation(files("libs/redsys-sdk-inApp-2.4.5.aar"))

    // Dependencias necesarias para el SDK de Redsys
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("androidx.webkit:webkit:1.14.0")
    implementation("com.android.volley:volley:1.2.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core:1.5.0")

    // Pagos con Stripe
    implementation("com.stripe:stripe-android:23.3.0")

    // Dependencias del lector de código de barras y la cámara
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")

    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha") // Para notificaciones
    implementation("com.composables:icons-lucide:1.0.0") // Iconos de lucide

    // Dependencias de firebase
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.android.gms:play-services-auth:21.5.0")

    // Dependencias para subir imágenes
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Dependencias para el chatbot
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    //implementation("com.google.firebase:firebase-firestore:26.0.2")
    implementation("androidx.room:room-runtime:2.8.3")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.compose.material3:material3:1.5.0-alpha08")
    implementation("androidx.compose.material:material-icons-extended:1.5.0-alpha08")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.ui.text)
    implementation(libs.firebase.crashlytics.buildtools)
    ksp("androidx.room:room-compiler:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}