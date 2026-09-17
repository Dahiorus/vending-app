import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spotless) apply false
}

// Type-safe `libs.xxx` accessors are bound lexically to the script that
// declares them and don't resolve inside `subprojects {}` (the receiver is
// dynamically a different project). Capture the catalog through the generic
// VersionCatalog API instead, and use `libsCatalog.findLibrary(...)` below.
val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

// Only the Gradle/Java backend modules (backend:domain, backend:application,
// backend:infrastructure) get the Java/Spring conventions below; the
// `frontend` project is a plain Node/npm project wrapped for the umbrella
// build (see frontend/build.gradle.kts) and must not receive them.
val backendSubprojects = subprojects.filter { it.path.startsWith(":backend:") }

allprojects {
    group = "me.dahiorus.project"
    version = "0.0.1-SNAPSHOT"
}

configure(backendSubprojects) {
    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libsCatalog.findVersion("java").get().requiredVersion))
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat(libsCatalog.findVersion("googleJavaFormat").get().requiredVersion)
        }
    }

    // `format`/`formatCheck` are project-agnostic aliases so `./gradlew format`
    // (used from the IDE and documented for contributors) works regardless of
    // which formatter Spotless is configured with underneath.
    tasks.register("format") {
        group = "formatting"
        description = "Formats the source code (alias for spotlessApply)."
        dependsOn("spotlessApply")
    }

    tasks.named("check") {
        dependsOn("spotlessCheck")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"(platform(libsCatalog.findLibrary("spring-boot-dependencies").get()))
        "annotationProcessor"(platform(libsCatalog.findLibrary("spring-boot-dependencies").get()))
        "testImplementation"(libsCatalog.findLibrary("spring-boot-starter-test").get())
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Root-level aggregator so `./gradlew format` formats every module (backend
// Java via Spotless/google-java-format, frontend TypeScript/HTML/CSS via
// Prettier — see frontend/build.gradle.kts) in one go.
tasks.register("format") {
    group = "formatting"
    description = "Formats the source code of all modules (backend + frontend)."
    dependsOn(backendSubprojects.map { "${it.path}:format" })
    dependsOn(":frontend:format")
}
