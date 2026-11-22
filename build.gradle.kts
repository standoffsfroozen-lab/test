plugins {
    java
}

group = "dev.cw"
version = "1.0.0"

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.jar {
    archiveFileName.set("CustomWorlds.jar")
}
