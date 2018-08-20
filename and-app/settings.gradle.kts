rootProject.name = "and-app"

val AGP_VERSION = "3.1.4"

buildscript {
  repositories {
    gradlePluginPortal()
    jcenter()
    google()
    mavenLocal()
  }
  dependencies {
    classpath("com.gradle:build-scan-plugin:1.15.1")
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