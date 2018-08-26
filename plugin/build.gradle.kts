/*
 *    Copyright 2018 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import org.gradle.cache.FileLock
import org.jetbrains.kotlin.android.synthetic.AndroidComponentRegistrar
import java.io.RandomAccessFile

plugins {
  `java-gradle-plugin`
  kotlin("jvm")
  `maven-publish`
}

gradlePlugin {
  plugins {
    create("android-components") {
      id = name
      implementationClass = "com.trevjonez.AndroidComponentsPlugin"
    }
  }
}

tasks.named("test").configure {
  this as Test
  useJUnitPlatform()

  systemProperty("testLibDir", "${rootDir.absolutePath}/and-lib")
  systemProperty("testAppDir", "${rootDir.absolutePath}/and-app")
  systemProperty("org.gradle.testkit.debug", false)

  inputs.files(
      "gradle.properties",
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
  compile(kotlin("stdlib-jdk8"))
  compile("com.android.tools.build:gradle")

  testCompile("org.assertj:assertj-core:3.11.0")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}
