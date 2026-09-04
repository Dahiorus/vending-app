// Wraps the Angular/npm project as a plain Gradle project so that
// `./gradlew build`/`check` at the repo root also builds and tests the
// frontend, alongside the backend:* Java modules (see settings.gradle.kts).
// No Node/npm Gradle plugin is used: npm itself (pinned via Volta, see
// package.json's `volta.node`) must already be resolvable on the PATH.
plugins {
    base
}

val npmInstall = tasks.register<Exec>("npmInstall") {
    description = "Installs frontend dependencies (npm ci)."
    group = "build setup"
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
    commandLine("npm", "ci")
}

val npmBuild = tasks.register<Exec>("npmBuild") {
    description = "Builds the Angular production bundle (npm run build)."
    group = "build"
    dependsOn(npmInstall)
    inputs.dir("src")
    inputs.file("angular.json")
    outputs.dir("dist/frontend")
    commandLine("npm", "run", "build")
}

val npmTest = tasks.register<Exec>("npmTest") {
    description = "Runs the frontend unit tests (npm test)."
    group = "verification"
    dependsOn(npmInstall)
    commandLine("npm", "test")
}

tasks.assemble {
    dependsOn(npmBuild)
}

tasks.check {
    dependsOn(npmTest)
}
