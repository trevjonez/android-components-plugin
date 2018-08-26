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

rootProject.name = "android-components"

include("plugin")
//includeBuild("and-app")
//includeBuild("and-lib")
//includeBuild("java-lib")

val AGP_VERSION = "3.1.4"
val KOTLIN_VERSION = "1.2.61"

pluginManagement {
  repositories {
    gradlePluginPortal()
    jcenter()
    google()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id.startsWith("com.android"))
        useModule("com.android.tools.build:gradle:$AGP_VERSION")
      if (requested.id.id.startsWith("org.jetbrains.kotlin"))
        useVersion(KOTLIN_VERSION)
    }
  }
}

gradle.allprojects {
  group = "com.trevjonez.android-components"
  version = "1.0-SNAPSHOT"

  repositories {
    google()
    jcenter()
  }

  configurations.all {
    resolutionStrategy {
      eachDependency {
        if (requested.group == "com.android.tools.build" && requested.name == "gradle")
          useVersion(AGP_VERSION)

        if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-stdlib-jre8")
          useTarget("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION")
      }
    }
  }
}

enableFeaturePreview("GRADLE_METADATA")
enableFeaturePreview("STABLE_PUBLISHING")

buildscript {
  repositories {
    gradlePluginPortal()
    jcenter()
  }
  dependencies {
    classpath("com.gradle:build-scan-plugin:1.15.1")
  }
}

gradle.rootProject {
  apply<com.gradle.scan.plugin.BuildScanPlugin>()

  configure<com.gradle.scan.plugin.BuildScanExtension> {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
  }
}