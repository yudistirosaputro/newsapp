plugins {
    alias(libs.plugins.buildlogic.android.library)
}

android {
    namespace = "com.blank.core"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.recyclerview)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment.ktx)

    // Lifecycle
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    api(libs.androidx.navigation.fragment.ktx)
    api(libs.androidx.navigation.ui.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
