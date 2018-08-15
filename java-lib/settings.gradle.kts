rootProject.name = "java-lib"

buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath("com.gradle:build-scan-plugin:1.15.1")
  }
}

enableFeaturePreview("GRADLE_METADATA")
enableFeaturePreview("STABLE_PUBLISHING")

gradle.rootProject {
  apply<com.gradle.scan.plugin.BuildScanPlugin>()

  configure<com.gradle.scan.plugin.BuildScanExtension> {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
  }
}