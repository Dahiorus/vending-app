plugins {
    alias(libs.plugins.spring.boot)
    `jvm-test-suite`
}

springBoot {
    mainClass.set("me.dahiorus.project.vending.VendingApplication")
}

// `intTest` is a dedicated JVM Test Suite (Gradle's recommended way to add
// a second, slower kind of test alongside the default `test` suite) for the
// *IT integration tests under src/intTest.
testing {
    suites {
        val test = getByName<JvmTestSuite>("test")

        register<JvmTestSuite>("intTest") {
            useJUnitJupiter()

            dependencies {
                implementation(project())
                implementation(testFixtures(project(":backend:domain")))
                // intTestImplementation does not extend implementation, so the BOM
                // platform applied to subprojects at the root does not propagate here.
                implementation(platform(libs.spring.boot.dependencies))
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.security.test) {
                    exclude(
                        group = "org.springframework.boot",
                        module = "spring-boot-starter-logging"
                    )
                }
                implementation(libs.h2)
                implementation(libs.quick.perf.junit5)
                implementation(libs.quick.perf.springboot2.sql.starter)
            }

            targets {
                all {
                    testTask.configure {
                        description = "Runs the integration tests."
                        shouldRunAfter(test)
                        // SecurityChainIT boots the full application context, allocating every
                        // ehcache off-heap cache defined in ehcache.xml (8 caches x 100MB); the
                        // JVM default MaxDirectMemorySize is too small for that on constrained
                        // environments.
                        jvmArgs("-XX:MaxDirectMemorySize=1200m")
                    }
                }
            }
        }
    }
}

// The infrastructure module declares its Spring Security/OAuth2/Jackson
// dependencies as `implementation` (not `api`), so `implementation(project())`
// alone does not expose them to intTest's compile/runtime classpath. Extending
// the suite's configurations from the main sourceSet's own configurations
// makes the full main classpath (including transitive `implementation` deps)
// visible, the same way the built-in `test` source set already works.
configurations.named("intTestImplementation") {
    extendsFrom(configurations.getByName("implementation"))
}
configurations.named("intTestRuntimeOnly") {
    extendsFrom(configurations.getByName("runtimeOnly"))
}

tasks.named("check") {
    dependsOn(testing.suites.named("intTest"))
}

// The jacoco plugin's `jacocoTestReport` only wires to `test` by default;
// this module's tests are split across `test` (unit) and `intTest`
// (SecurityChainIT and friends), so the report is overridden here to merge
// both execution data files into a single unified coverage report instead
// of reporting unit-test coverage only.
tasks.named<JacocoReport>("jacocoTestReport") {
    executionData(tasks.named("test").get(), tasks.named("intTest").get())
    dependsOn(tasks.named("test"), tasks.named("intTest"))
}

dependencies {
    implementation(project(":backend:domain"))
    runtimeOnly(project(":backend:application"))
    testImplementation(testFixtures(project(":backend:domain")))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.data.commons)
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.spring.boot.starter.hateoas)
    implementation(libs.spring.boot.starter.web) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    annotationProcessor(libs.spring.boot.configuration.processor)
    implementation(libs.nimbus.jose.jwt)

    // developmentOnly is a leaf configuration that does not extend
    // implementation, so it needs its own BOM import.
    developmentOnly(platform(libs.spring.boot.dependencies))
    developmentOnly(libs.spring.boot.devtools)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.commons.lang3)
    implementation(libs.commons.collections4)
    implementation(libs.commons.io)

    implementation(libs.spring.boot.starter.cache)
    implementation(variantOf(libs.ehcache) { classifier("jakarta") })

    testImplementation(libs.spring.security.test) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    testImplementation(libs.h2)
}
