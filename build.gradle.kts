import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jreleaser.model.Active
import org.jreleaser.model.Signing.Mode

plugins {
    `java-library`
    application
    `maven-publish`
    jacoco
    id("org.jreleaser") version "1.21.0"
}

group = "tanin.jmigrate"
version = "0.4.0"

description = "JMigrate: database migration management"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = true
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.renomad:minum:8.3.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.postgresql:postgresql:42.7.8")
    testImplementation("org.xerial:sqlite-jdbc:3.51.0.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("started", "passed", "skipped", "failed")
        showStandardStreams = true
        showStackTraces = true
        showExceptions = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
    }

}

application {
    mainClass.set("tanin.jmigrate.JMigrate")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.tanin47"
            artifactId = "jmigrate"
            version = project.version.toString()
            artifact(tasks.jar)
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("JMigrate")
                description.set("Simple and reliable database migration management")
                url.set("https://github.com/tanin47/jmigrate")
                inceptionYear.set("2025")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id.set("tanin47")
                        name.set("Tanin Na Nakorn")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/tanin47/jmigrate.git")
                    developerConnection.set("scm:git:ssh://github.com/tanin47/jmigrate.git")
                    url.set("http://github.com/tanin47/jmigrate")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    signing {
        active = Active.ALWAYS
        armored = true
        mode = if (System.getenv("CI") != null) Mode.MEMORY else Mode.COMMAND
        command {
            executable = "/opt/homebrew/bin/gpg"
        }
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive("ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

// For CI validation.
tasks.register("printVersion") {
    doLast {
        print("$version")
    }
}
