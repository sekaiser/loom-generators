plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.4.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jtrim2:jtrim-collections:2.0.6")
    implementation("org.jtrim2:jtrim-concurrent:2.0.6")
    implementation("org.jtrim2:jtrim-utils:2.0.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs = listOf(
            "--add-exports", "java.base/jdk.internal.vm=loom.generators",
            "--add-exports", "java.base/jdk.internal.access=loom.generators",
            "--enable-preview"
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs = listOf(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED",
            "--add-exports", "java.base/jdk.internal.access=ALL-UNNAMED",
            "--enable-preview"
    )
}

tasks.withType<Wrapper> {
    gradleVersion = "8.2.1"
}

tasks.withType<Jar> {
    manifest {
        attributes("Automatic-Module-Name" to "org.jtrim2.concurrent")
        attributes("Automatic-Module-Name" to "jtrim.collections")
        attributes("Automatic-Module-Name" to "jtrim.utils")
//        attributes("Automatic-Module-Name" to "org.jtrim2.collections")
    }
}

extraJavaModuleInfo {
    automaticModule("jtrim-utils-2.0.6.jar", "jtrim.utils")
    automaticModule("jtrim-collections-2.0.6.jar", "jtrim.collections")
    automaticModule("jtrim-concurrent-2.0.6.jar", "jtrim.concurrent")
}

