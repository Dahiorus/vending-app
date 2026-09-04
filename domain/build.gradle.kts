plugins {
    `java-test-fixtures`
}

dependencies {
    // testFixturesImplementation does not extend implementation, so the BOM
    // platform applied to subprojects at the root does not propagate here.
    testFixturesImplementation(platform(libs.spring.boot.dependencies))
    testFixturesImplementation(libs.commons.lang3)
    testImplementation(libs.commons.lang3)
}
