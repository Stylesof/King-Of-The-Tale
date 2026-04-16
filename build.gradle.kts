plugins {
    id("java")
}

group = "styles"
version = "0.76.62-BETA"

repositories {
    mavenCentral()
    maven {
        name = "hytale"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.hypixel.hytale:Server:+")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("VERSION", project.version);

    filesMatching("manifest.json") {
        expand("VERSION" to project.version);
    }
}

tasks.jar {
    doFirst {
        delete(fileTree(layout.buildDirectory.dir("libs")) {
            include("*.jar")
        })
    }
}