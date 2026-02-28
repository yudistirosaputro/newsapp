import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

class AndroidTestAggregatorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(JacocoPlugin::class.java)

            // Register root test task
            tasks.register("runAllTests") {
                group = "verification"
                description = "Runs all unit tests across all modules"

                dependsOn(subprojects.flatMap { subproject ->
                    subproject.tasks.matching { it.name == "testDebugUnitTest" }
                })
            }

            // Register aggregated coverage report task
            tasks.register<JacocoReport>("generateAggregatedCoverageReport") {
                group = "verification"
                description = "Generates aggregated Jacoco coverage report from all modules"

                dependsOn("runAllTests")

                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated"))
                }

                // Collect class directories from all subprojects
                val classDirs = subprojects.map { subproject ->
                    subproject.layout.buildDirectory.dir("tmp/kotlin-classes/debug").get().asFileTree
                        .matching {
                            exclude(
                                "**/R.class",
                                "**/R$*.class",
                                "**/BuildConfig.*",
                                "**/Manifest*.*",
                                "**/*Test*.*",
                                "android/**/*.*",
                                "**/di/**/*.*",
                                "**/databinding/**/*.*",
                                "**/DataBinding*.*",
                                "**/*_Impl*.*"
                            )
                        }
                }

                // Collect source directories from all subprojects
                val sourceDirs = subprojects.flatMap { subproject ->
                    listOf(
                        subproject.file("src/main/java"),
                        subproject.file("src/main/kotlin")
                    ).filter { it.exists() }
                }

                // Collect execution data from all subprojects
                val execFiles = subprojects.mapNotNull { subproject ->
                    val execFile = subproject.layout.buildDirectory
                        .file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
                        .get().asFile
                    if (execFile.exists()) execFile else null
                }

                classDirectories.setFrom(classDirs)
                sourceDirectories.setFrom(sourceDirs)
                executionData.setFrom(execFiles)
            }

            // Task to print test summary
            tasks.register("testSummary") {
                group = "verification"
                description = "Prints a summary of all test results"

                dependsOn("runAllTests")

                doLast {
                    val testResults = mutableListOf<Triple<String, Int, Int>>() // module, passed, failed

                    subprojects.forEach { subproject ->
                        val testResultDir = subproject.layout.buildDirectory
                            .dir("test-results/testDebugUnitTest").get().asFile

                        if (testResultDir.exists()) {
                            val xmlFiles = testResultDir.listFiles { _, name ->
                                name.endsWith(".xml")
                            } ?: emptyArray()

                            var passed = 0
                            var failed = 0

                            xmlFiles.forEach { xmlFile ->
                                val content = xmlFile.readText()
                                val tests = Regex("tests=\"(\\d+)\"").find(content)?.groupValues?.get(1)?.toInt() ?: 0
                                val failures = Regex("failures=\"(\\d+)\"").find(content)?.groupValues?.get(1)?.toInt() ?: 0
                                val errors = Regex("errors=\"(\\d+)\"").find(content)?.groupValues?.get(1)?.toInt() ?: 0

                                passed += tests - failures - errors
                                failed += failures + errors
                            }

                            if (passed + failed > 0) {
                                testResults.add(Triple(subproject.name, passed, failed))
                            }
                        }
                    }

                    println("\n" + "=".repeat(70))
                    println("                    TEST SUMMARY REPORT")
                    println("=".repeat(70))
                    println(String.format("%-25s %10s %10s %10s", "Module", "Passed", "Failed", "Total"))
                    println("-".repeat(70))

                    var totalPassed = 0
                    var totalFailed = 0

                    testResults.sortedBy { it.first }.forEach { (module, passed, failed) ->
                        val total = passed + failed
                        println(String.format("%-25s %10d %10d %10d", module, passed, failed, total))
                        totalPassed += passed
                        totalFailed += failed
                    }

                    println("-".repeat(70))
                    println(String.format("%-25s %10d %10d %10d", "TOTAL", totalPassed, totalFailed, totalPassed + totalFailed))
                    println("=".repeat(70))

                    if (totalFailed > 0) {
                        println("\n❌ $totalFailed test(s) FAILED")
                    } else {
                        println("\n✅ All tests PASSED!")
                    }
                    println()
                }
            }

            // Combined task for CI
            tasks.register("runAllTestsWithCoverage") {
                group = "verification"
                description = "Runs all tests and generates aggregated coverage report"

                dependsOn("runAllTests")
                dependsOn("generateAggregatedCoverageReport")
                dependsOn("testSummary")
            }
        }
    }
}
