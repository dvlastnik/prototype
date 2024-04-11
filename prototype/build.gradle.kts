plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")
    implementation("com.squareup:kotlinpoet:1.15.2")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.dvlastnik"
            artifactId = "compose-rapid-prototyping"
            version = "1.0.0"

            from(components["java"])
        }
    }
}