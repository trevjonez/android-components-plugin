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

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
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
    val buildResult = GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":publish")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)

    assertThat(File(testLibDir, "build/.m2/com/trevjonez/")).satisfies { m2Root ->
      SoftAssertions().also { softly ->
        softly.assertThat(File(m2Root, "and-lib/0.1.0/and-lib-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib/0.1.0/and-lib-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib/0.1.0/and-lib-0.1.0.aar")).doesNotExist()

        softly.assertThat(File(m2Root, "and-lib_debug/0.1.0/and-lib_debug-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib_debug/0.1.0/and-lib_debug-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib_debug/0.1.0/and-lib_debug-0.1.0.aar")).exists()

        softly.assertThat(File(m2Root, "and-lib_release/0.1.0/and-lib_release-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib_release/0.1.0/and-lib_release-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib_release/0.1.0/and-lib_release-0.1.0.aar")).exists()
      }.assertAll()
    }
  }

  @Test
  internal fun `lib can specify artifact id and not be overwritten`() {
    val buildResult = GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-id-spec.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":publish")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)

    assertThat(File(testLibDir, "build/.m2/com/trevjonez")).satisfies { m2Root ->
      SoftAssertions().also { softly ->
        softly.assertThat(File(m2Root, "artId/0.1.0/artId-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "artId/0.1.0/artId-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "artId/0.1.0/artId-0.1.0.aar")).doesNotExist()

        softly.assertThat(File(m2Root, "artId_debug/0.1.0/artId_debug-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "artId_debug/0.1.0/artId_debug-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "artId_debug/0.1.0/artId_debug-0.1.0.aar")).exists()

        softly.assertThat(File(m2Root, "artId_release/0.1.0/artId_release-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "artId_release/0.1.0/artId_release-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "artId_release/0.1.0/artId_release-0.1.0.aar")).exists()
      }.assertAll()
    }
  }

  @Test
  internal fun `lib can publish build type and single flavor dimension variants as expected`() {
    val buildResult = GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":publish")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)

    assertThat(File(testLibDir, "build/.m2/com/trevjonez/")).satisfies { m2Root ->
      SoftAssertions().also { softly ->
        softly.assertThat(File(m2Root, "and-lib/0.1.0/and-lib-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib/0.1.0/and-lib-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib/0.1.0/and-lib-0.1.0.aar")).doesNotExist()

        softly.assertThat(File(m2Root, "and-lib_red_debug/0.1.0/and-lib_red_debug-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib_red_debug/0.1.0/and-lib_red_debug-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib_red_debug/0.1.0/and-lib_red_debug-0.1.0.aar")).exists()

        softly.assertThat(File(m2Root, "and-lib_blue_debug/0.1.0/and-lib_blue_debug-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib_blue_debug/0.1.0/and-lib_blue_debug-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib_blue_debug/0.1.0/and-lib_blue_debug-0.1.0.aar")).exists()

        softly.assertThat(File(m2Root, "and-lib_red_release/0.1.0/and-lib_red_release-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib_red_release/0.1.0/and-lib_red_release-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib_red_release/0.1.0/and-lib_red_release-0.1.0.aar")).exists()

        softly.assertThat(File(m2Root, "and-lib_blue_release/0.1.0/and-lib_blue_release-0.1.0.module")).exists()
        softly.assertThat(File(m2Root, "and-lib_blue_release/0.1.0/and-lib_blue_release-0.1.0.pom")).exists()
        softly.assertThat(File(m2Root, "and-lib_blue_release/0.1.0/and-lib_blue_release-0.1.0.aar")).exists()
      }.assertAll()
    }
  }

  @Test
  internal fun `app can consume lib via redirecting pom`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    val buildResult = GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("dependencies", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":assemble")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test
  internal fun `app can consume lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    val buildResult = GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-c", "settings-metadata.gradle.kts", "dependencies", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":assemble")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)

    assertThat(buildResult.output).contains("""
        |debugCompileClasspath - Resolved configuration for compilation for variant: debug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |debugRuntimeClasspath - Resolved configuration for runtime for variant: debug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |implementation - Implementation only dependencies for 'main' sources. (n)
        |\--- com.trevjonez:and-lib:0.1.0 (n)
    """.trimMargin()).contains("""
        |releaseCompileClasspath - Resolved configuration for compilation for variant: release
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |releaseRuntimeClasspath - Resolved configuration for runtime for variant: release
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin())
  }

  @Test
  internal fun `app can consume single flavor dimension variants lib via redirecting pom`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    val buildResult = GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":assemble")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test
  internal fun `app can consume single flavor dimension variants lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    val buildResult = GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-c", "settings-metadata.gradle.kts", "dependencies", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":assemble")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)

    assertThat(buildResult.output).contains("""
        |debugCompileClasspath - Resolved configuration for compilation for variant: debug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |debugRuntimeClasspath - Resolved configuration for runtime for variant: debug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |implementation - Implementation only dependencies for 'main' sources. (n)
        |\--- com.trevjonez:and-lib:0.1.0 (n)
    """.trimMargin()).contains("""
        |releaseCompileClasspath - Resolved configuration for compilation for variant: release
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |releaseRuntimeClasspath - Resolved configuration for runtime for variant: release
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin())
  }

  @Test
  internal fun `app with single flavor dimension variants can consume single flavor dimension variants lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    val buildResult = GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors.gradle.kts", "-c", "settings-metadata.gradle.kts", "dependencies", "assemble", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.task(":assemble")!!.outcome)
        .isEqualTo(TaskOutcome.SUCCESS)

    assertThat(buildResult.output).contains("""
        |blueDebugCompileClasspath - Resolved configuration for compilation for variant: blueDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueDebugRuntimeClasspath - Resolved configuration for runtime for variant: blueDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueReleaseCompileClasspath - Resolved configuration for compilation for variant: blueRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueReleaseRuntimeClasspath - Resolved configuration for runtime for variant: blueRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |implementation - Implementation only dependencies for 'main' sources. (n)
        |\--- com.trevjonez:and-lib:0.1.0 (n)
    """.trimMargin()).contains("""
        |redDebugCompileClasspath - Resolved configuration for compilation for variant: redDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redDebugRuntimeClasspath - Resolved configuration for runtime for variant: redDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redReleaseCompileClasspath - Resolved configuration for compilation for variant: redRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redReleaseRuntimeClasspath - Resolved configuration for runtime for variant: redRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin())
  }

  @Test
  internal fun `app with multi flavor dimension variants can consume multi flavor dimension variants lib via module metadata`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors-many.gradle.kts", "publish", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    val buildResult = GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("-b", "build-flavors-many.gradle.kts", "-c", "settings-metadata.gradle.kts", "assemble", "dependencies", "--stacktrace", "--build-cache", "--scan")
        .withPluginClasspath()
        .build()

    assertThat(buildResult.output).contains("""
        |blueCircleDebugCompileClasspath - Resolved configuration for compilation for variant: blueCircleDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_circle_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueCircleDebugRuntimeClasspath - Resolved configuration for runtime for variant: blueCircleDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_circle_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueCircleReleaseCompileClasspath - Resolved configuration for compilation for variant: blueCircleRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_circle_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueCircleReleaseRuntimeClasspath - Resolved configuration for runtime for variant: blueCircleRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_circle_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueSquareDebugCompileClasspath - Resolved configuration for compilation for variant: blueSquareDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_square_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueSquareDebugRuntimeClasspath - Resolved configuration for runtime for variant: blueSquareDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_square_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueSquareReleaseCompileClasspath - Resolved configuration for compilation for variant: blueSquareRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_square_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |blueSquareReleaseRuntimeClasspath - Resolved configuration for runtime for variant: blueSquareRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_blue_square_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redCircleDebugCompileClasspath - Resolved configuration for compilation for variant: redCircleDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_circle_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redCircleDebugRuntimeClasspath - Resolved configuration for runtime for variant: redCircleDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_circle_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redCircleReleaseCompileClasspath - Resolved configuration for compilation for variant: redCircleRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_circle_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redCircleReleaseRuntimeClasspath - Resolved configuration for runtime for variant: redCircleRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_circle_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redSquareDebugCompileClasspath - Resolved configuration for compilation for variant: redSquareDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_square_debug:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redSquareDebugRuntimeClasspath - Resolved configuration for runtime for variant: redSquareDebug
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_square_debug:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redSquareReleaseCompileClasspath - Resolved configuration for compilation for variant: redSquareRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_square_release:0.1.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin()).contains("""
        |redSquareReleaseRuntimeClasspath - Resolved configuration for runtime for variant: redSquareRelease
        |\--- com.trevjonez:and-lib:0.1.0
        |     \--- com.trevjonez:and-lib_red_square_release:0.1.0
        |          +--- com.squareup.moshi:moshi:1.6.0
        |          |    \--- com.squareup.okio:okio:1.14.0
        |          \--- io.reactivex.rxjava2:rxjava:2.2.0
        |               \--- org.reactivestreams:reactive-streams:1.0.2
    """.trimMargin())
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
