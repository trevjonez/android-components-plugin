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
  internal fun `lib can publish artifacts as expected`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .build()
  }

  @Test
  internal fun `app can consume lib`() {
    GradleRunner.create()
        .withProjectDir(testLibDir)
        .forwardOutput()
        .withArguments("publish", "--stacktrace", "--build-cache", "--scan")
        .build()

    GradleRunner.create()
        .withProjectDir(testAppDir)
        .forwardOutput()
        .withArguments("assemble", "--stacktrace", "--build-cache", "--scan")
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
