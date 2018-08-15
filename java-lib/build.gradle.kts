plugins {
  `java-library`
  `maven-publish`
}

group = "com.trevjonez"
version = "0.1.0"

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
  publications {
    register<MavenPublication>("jar") {
      from(components.getByName("java"))
    }
  }
}