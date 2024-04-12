plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")
    implementation("com.squareup:kotlinpoet:1.15.2")
}

group = "com.github.dvlastnik"
version = "1.0.1"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.github.dvlastnik"
            artifactId = "prototype"
            version = "1.0.1"
        }
    }
}

tasks.publishToMavenLocal {
    dependsOn("build")
}