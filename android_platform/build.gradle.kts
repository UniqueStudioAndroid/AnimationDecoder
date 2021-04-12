plugins {
    id("com.android.library")
    id("kotlin-android")
}


android {
    compileSdkVersion(Dep.Build.COMPILE_SDK_VERSION)
    buildToolsVersion(Dep.Build.BUILD_TOOLS_VERSION)

    defaultConfig {
        minSdkVersion(Dep.Build.MIN_SDK_VERSION)
        targetSdkVersion(Dep.Build.TARGET_SDK_VERSION)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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
    buildFeatures {
        viewBinding = true
    }
}


dependencies {

    api(project(":api"))
    implementation(project(":core"))
    implementation(project(":apng"))

    implementation(kotlin("stdlib", Dep.Kt.KOTLIN_VERSION))
    testImplementation(Dep.Test.JUNIT)

    androidTestImplementation(Dep.Test.EXT_JUNIT)
    androidTestImplementation(Dep.Test.ESPRESSO)
}
