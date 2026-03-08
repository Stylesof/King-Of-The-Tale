plugins {
    id("java")
}

group = "styles"
version = "0.0.1-BETA"

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