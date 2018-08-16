plugins {
  id("android-components")
  id("com.android.library")
  `maven-publish`
}

group = "com.trevjonez"
version = "0.1.0"

repositories {
  google()
  jcenter()
}

android {
  compileSdkVersion(28)
  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(28)
  }
  defaultPublishConfig("blueRelease")
  flavorDimensions("color")
  productFlavors {
    create("red") {

    }
    create("blue") {

    }
  }
}

dependencies {
  api("io.reactivex.rxjava2:rxjava:2.2.0")
  implementation("com.squareup.moshi:moshi:1.6.0")
}

publishing {
  repositories {
    maven {
      name = "buildDir"
      url = uri("${buildDir.absolutePath}/.m2")
    }
  }
}