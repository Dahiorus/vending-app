dependencies {
    api(project(":domain"))

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.commons.lang3)
    implementation(libs.commons.collections4)
    implementation(libs.commons.io)
}
