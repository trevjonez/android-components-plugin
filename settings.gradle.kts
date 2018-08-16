rootProject.name = "android-components"

include("plugin")
//includeBuild("and-lib")
//includeBuild("java-lib")

val AGP_VERSION = "3.1.3"

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
          useTarget("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.60")
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