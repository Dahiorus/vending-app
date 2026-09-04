import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.spring.boot) apply false
}

// Type-safe `libs.xxx` accessors are bound lexically to the script that
// declares them and don't resolve inside `subprojects {}` (the receiver is
// dynamically a different project). Capture the catalog through the generic
// VersionCatalog API instead, and use `libsCatalog.findLibrary(...)` below.
val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

allprojects {
    group = "me.dahiorus.project"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libsCatalog.findVersion("java").get().requiredVersion))
        }
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
