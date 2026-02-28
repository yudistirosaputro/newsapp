import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.tasks.JacocoReport

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")

            extensions.configure<LibraryExtension> {
                buildTypes {
                    getByName("debug") {
                        enableUnitTestCoverage = true
                    }
                }

                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            configureJacoco()

            dependencies {
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("mockk-android").get())
                add("testImplementation", libs.findLibrary("mockk-agent-jvm").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("testImplementation", libs.findLibrary("androidx-arch-core-testing").get())
                add("testImplementation", libs.findLibrary("androidx-paging-testing").get())
            }
        }
    }

    private fun Project.configureJacoco() {
        tasks.withType<JacocoReport> {
            dependsOn("testDebugUnitTest")

            reports {
                xml.required.set(true)
                html.required.set(true)
            }

            classDirectories.setFrom(
                fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                    exclude(
                        "**/R.class",
                        "**/R$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                        "android/**/*.*",
                        "**/di/**/*.*",
                        "**/databinding/**/*.*",
                        "**/android/databinding/*",
                        "**/DataBinding*.*",
                        "**/*_Impl*.*"
                    )
                }
            )

            sourceDirectories.setFrom(
                "${project.projectDir}/src/main/java",
                "${project.projectDir}/src/main/kotlin"
            )

            executionData.setFrom(
                fileTree(layout.buildDirectory) {
                    include(
                        "jacoco/testDebugUnitTest.exec",
                        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
                    )
                }
            )
        }

        tasks.register("jacocoFullReport", JacocoReport::class.java) {
            group = "verification"
            description = "Generates full Jacoco coverage report for all modules"
            
            dependsOn("testDebugUnitTest")

            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }
    }
}
