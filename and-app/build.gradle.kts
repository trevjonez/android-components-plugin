plugins {
  id("android-components")
  id("com.android.application")
  `maven-publish`
}

group = "com.trevjonez"
version = "0.1.0"

repositories {
  google()
  jcenter()
  maven {
    url = uri("../and-lib/build/.m2")
  }
}

android {
  compileSdkVersion(28)
  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(28)
    missingDimensionStrategy("color", "blue")
  }
}

dependencies {
  implementation("com.trevjonez:and-lib:0.1.0") {
    isChanging = true
  }
}

configurations {
  "implementation" {
    resolutionStrategy {
      cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }
  }
}

publishing {
  repositories {
    maven {
      name = "buildDir"
      url = uri("${buildDir.absolutePath}/.m2")
    }
  }
}