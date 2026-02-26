plugins {
    alias(libs.plugins.buildlogic.android.library)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.blank.navigation"
}

dependencies {
    implementation(libs.serialization.json)
}
