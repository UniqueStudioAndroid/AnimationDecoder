
object Dep {

    object Build {
        const val APPLICATION_ID = "com.hustunique.myapplication"
        const val MIN_SDK_VERSION = 21
        const val TARGET_SDK_VERSION = 30
        const val ANDROID_TOOLS = "com.android.tools.build:gradle:4.1.3"
    }

    object Kt {
        const val KOTLIN_VERSION = "1.4.31"
        const val COROUTINE = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9"
    }

//    testImplementation 'junit:junit:4.+'
//    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
//    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
    object AndroidX {
        const val CORE = "org.jetbrains.kotlin:kotlin-stdlib:1.4.31"
        const val APPCOMPAT = "androidx.appcompat:appcompat:1.2.0"
        const val MATERIAL = "com.google.android.material:material:1.3.0"
        const val CONSTRAINTLAYOUT = "androidx.constraintlayout:constraintlayout:2.0.4"
        const val LIVEDATA = "androidx.lifecycle:lifecycle-livedata:2.3.0"
    }

}