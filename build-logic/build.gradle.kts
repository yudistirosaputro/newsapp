plugins {
    `kotlin-dsl`
}

group = "buildlogic"

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    implementation(libs.jacoco.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "buildlogic.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "buildlogic.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "buildlogic.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidRoom") {
            id = "buildlogic.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidTest") {
            id = "buildlogic.android.test"
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("testAggregator") {
            id = "buildlogic.test.aggregator"
            implementationClass = "AndroidTestAggregatorPlugin"
        }
    }
}
