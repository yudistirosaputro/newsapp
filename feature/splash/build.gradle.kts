plugins {
    alias(libs.plugins.buildlogic.android.feature)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.blank.feature.splash"
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
