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

package com.trevjonez

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class AndroidComponentsPluginTest {

  val testLibDir by systemProperty { File(it) }
  val testAppDir by systemProperty { File(it) }

  @BeforeEach
  internal fun setUp() {
    File(testLibDir, "build").deleteRecursively()
  }

  @Test
  internal fun `lib can publish build type variants as expected`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }

  @Test
  internal fun `lib can publish build type and single flavor dimension variants as expected`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }

  @Test
  internal fun `app can consume lib via redirecting pom`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }

  @Test
  internal fun `app can consume lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-c", "settings-metadata.gradle.kts", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }

  @Test
  internal fun `app can consume single flavor dimension variants lib via redirecting pom`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }

  @Test
  internal fun `app can consume single flavor dimension variants lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-c", "settings-metadata.gradle.kts", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }

  @Test
  internal fun `app with single flavor dimension variants can consume single flavor dimension variants lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "-c", "settings-metadata.gradle.kts", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }

  @Test
  internal fun `app with multi flavor dimension variants can consume multi flavor dimension variants lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors-many.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors-many.gradle.kts", "-c", "settings-metadata.gradle.kts", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()
  }
}

inline fun <R, T : Any> systemProperty(
    crossinline conversion: (String) -> T
): ReadOnlyProperty<R, T> {
  return object : ReadOnlyProperty<R, T> {
    override fun getValue(thisRef: R, property: KProperty<*>): T {
      return conversion(System.getProperty(property.name)!!)
    }
  }
}
