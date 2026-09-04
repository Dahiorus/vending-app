plugins {
    alias(libs.plugins.spring.boot)
}

springBoot {
    mainClass.set("me.dahiorus.project.vending.VendingApplication")
}

val intTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

configurations["intTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["intTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

dependencies {
    implementation(project(":domain"))
    runtimeOnly(project(":application"))
    testImplementation(testFixtures(project(":domain")))

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

val intTestTask = tasks.register<Test>("intTest") {
    description = "Runs the integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = intTest.output.classesDirs
    classpath = intTest.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(intTestTask)
}
