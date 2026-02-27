plugins {
    alias(libs.plugins.buildlogic.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.blank.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.paging.common)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
