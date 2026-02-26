plugins {
    alias(libs.plugins.buildlogic.android.library)
}

android {
    namespace = "com.blank.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.paging.common)
}
