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

rootProject.name = "and-app"

val AGP_VERSION: String by settings

buildscript {
  repositories {
    gradlePluginPortal()
    jcenter()
    google()
    mavenLocal()
  }
  dependencies {
    val GRADLE_SCAN_VERSION: String by settings
    classpath("com.gradle:build-scan-plugin:$GRADLE_SCAN_VERSION")
  }
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    jcenter()
    google()
    mavenLocal()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id.startsWith("com.android"))
        useModule("com.android.tools.build:gradle:$AGP_VERSION")
    }
  }
}

enableFeaturePreview("STABLE_PUBLISHING")
enableFeaturePreview("IMPROVED_POM_SUPPORT")

gradle.rootProject {
  apply<com.gradle.scan.plugin.BuildScanPlugin>()

  configure<com.gradle.scan.plugin.BuildScanExtension> {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
  }
}