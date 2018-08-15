import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-gradle-plugin`
  id("nebula.kotlin") version "1.2.60"
  `maven-publish`
  `kotlin-dsl`
}

buildscript {
  dependencies {
    classpath("com.android.tools.build:gradle:3.3.0-alpha05")
  }
}

gradlePlugin {
  plugins {
    create("android-components") {
      id = name
      implementationClass = "com.trevjonez.AndroidComponentsPlugin"
    }
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()

  systemProperty("testLibDir", "${rootDir.absolutePath}/and-lib")
  systemProperty("testAppDir", "${rootDir.absolutePath}/and-app")

  inputs.files(
      "../and-lib/build.gradle.kts",
      "../and-lib/build-flavors.gradle.kts",
      "../and-lib/settings.gradle.kts",
      "../and-app/build.gradle.kts",
      "../and-app/settings.gradle.kts")
  inputs.dir("../and-lib/src")
  inputs.dir("../and-app/src")
  outputs.dir("../and-lib/build")
  outputs.dir("../and-app/build")
}

dependencies {
  compile("com.android.tools.build:gradle:3.3.0-alpha05")

  testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}
