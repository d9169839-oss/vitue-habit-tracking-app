plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

// Keep upload-key material outside the repository. These Gradle properties can live in
// ~/.gradle/gradle.properties on a developer machine or in protected CI secrets.
val uploadStoreFile = providers.gradleProperty("VITUE_UPLOAD_STORE_FILE").orNull
val uploadStorePassword = providers.gradleProperty("VITUE_UPLOAD_STORE_PASSWORD").orNull
val uploadKeyAlias = providers.gradleProperty("VITUE_UPLOAD_KEY_ALIAS").orNull
val uploadKeyPassword = providers.gradleProperty("VITUE_UPLOAD_KEY_PASSWORD").orNull
val hasUploadSigning = listOf(uploadStoreFile, uploadStorePassword, uploadKeyAlias, uploadKeyPassword)
    .all { !it.isNullOrBlank() }

android {
    namespace = "com.virtue.habittracker"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.virtue.habittracker"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"720151151343-rugitfne5oksl9l2jn4m93s3rmujt7oo.apps.googleusercontent.com\"")
    }
    signingConfigs {
        create("release") {
            if (hasUploadSigning) {
                storeFile = file(uploadStoreFile!!)
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            // Keep shrinking disabled until release keep rules and mapping retention are tested.
            isMinifyEnabled = false
            // CI can compile the release bundle without secrets; Play uploads must use the upload key.
            if (hasUploadSigning) signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    // Firestore provides per-user cloud persistence and offline write queuing.
    implementation(libs.firebase.firestore)
    implementation(libs.coroutines.play.services)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
