plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ec.edu.utn.example.gestorproyectos"
    compileSdk = 35

    defaultConfig {
        applicationId = "ec.edu.utn.example.gestorproyectos"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "USER_EMAIL",    "\"pankey.ibarra@gmail.com\"")
        buildConfigField("String", "CLIENT_ID",     "\"192282288096-u9r7es2bg7js56q1k3c8i7tncvi0mnq8.apps.googleusercontent.com\"")
        buildConfigField("String", "CLIENT_SECRET", "\"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\"")
        buildConfigField("String", "REFRESH_TOKEN", "\"1//04SAkNR_lk4M6CgYIARAAGAQSNwF-L9Ir86OZRosnySypsTpESNRS9ZBJnNipdz9lN9L_4DOQBxebtBvaZInJF8T5b78ZNXfRN8w\"")
        buildConfigField("String", "SCOPE",         "\"https://mail.google.com/\"")
    }

    packaging {
        resources {
            // keep the first copy, ignore the rest
            pickFirsts += "META-INF/INDEX.LIST"
            pickFirsts += "META-INF/DEPENDENCIES"

            // (optional) drop other licence-type meta files you don’t care about
            excludes += "META-INF/*.md"
        }
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
        buildConfig = true                             // ← fuerza la generación
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.core.ktx)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.google.auth.oauth2.http) // versión actualizada: 1.34.0
    implementation(libs.google.api.client)
    implementation(libs.google.http.client.gson)
    implementation(libs.jakarta.mail)
    implementation(libs.dotenv.java)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
