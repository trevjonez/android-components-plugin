 plugins {
  id("com.android.library")
  `maven-publish`
  id("android-components") version System.getProperty("pluginVersion", "1.0-SNAPSHOT")
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