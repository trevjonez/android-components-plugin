# Android-Components-Plugin

Variant aware artifact publishing for android projects.

## Preface

Gradle and the [Android Gradle Plugin(s)] (AGP) provide a variant aware 
dependency resolution when depending on project modules directly. 
This is lost entirely when publishing maven style artifacts via existing methods. 
<sup>[ [1], [2], [3], [4], [5] ]</sup>

This plugin introduces support for the new [Gradle module metadata specification]
which utilizes the [Gradle component API] to expose the same variant aware
dependency resolution when depending on published artifacts. All without the 
usual ceremony required to publish the variant artifacts and greatly simplifies 
depending on the correct variants from downstream project configurations.

## Installation and Configuration

The plugin is available via the [Gradle Plugin Portal]

```kotlin
plugins {
  `maven-publish`
  id("com.android.library")
  id("com.trevjonez.android-components") version "0.1.0"
}

publishing {
  repositories {
    maven {
      name = "CompanyMaven"
      url = uri("https://maven.company.xyz")
      credentials {...}
    }
  }
}
```

`android-components` ties the configuration of the applied [AGP] to configure 
the [Maven-Publish plugin]. Publications are created for each variant that 
passes the [AGP filtering DSL]. A sources jar task and artifact is automatically
registered for each variant.

By default the maven coordinates are pulled from the project group, name, and version. 

Source publishing can be disabled, or primary artifact id overridden via the 
[AndroidComponentsExtension]:
```kotlin
androidComponents {
  publishSources = false
  artifactId = "CustomId"
}
```

## Artifact Consumption
#### .module metadata

In order to consume the [.module][Gradle module metadata specification] metadata
in downstream projects we must enable it via gradle metadata feature preview in
the settings file.

```kotlin
enableFeaturePreview("GRADLE_METADATA")
```

Then add the dependency as usual.
```kotlin
dependencies {
  implementation("com.example:lib:1.2.3")
}
```

#### .pom metadata

The `android-components` plugin will also generate a redirecting POM file so the
main artifact coordinates point to the variant that was set as the 
[default publishing configuration on the AGP DSL]. In this way the plugin provides
support equivalent to most other publishing/consumption options.

[1]: https://maven.apache.org/pom.html
[2]: https://docs.gradle.org/current/userguide/maven_plugin.html
[3]: https://docs.gradle.org/current/userguide/publishing_maven.html
[4]: https://github.com/dcendents/android-maven-gradle-plugin
[5]: https://github.com/wupdigital/android-maven-publish

[Gradle module metadata specification]: https://github.com/gradle/gradle/blob/master/subprojects/docs/src/docs/design/gradle-module-metadata-specification.md
[Gradle component API]: https://docs.gradle.org/current/javadoc/index.html?org/gradle/api/component/package-summary.html
[Gradle Plugin Portal]: https://plugins.gradle.org/
[AGP]: https://developer.android.com/studio/releases/gradle-plugin
[Android Gradle Plugin(s)]: https://developer.android.com/studio/releases/gradle-plugin
[AGP filtering DSL]: https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.LibraryExtension.html#com.android.build.gradle.LibraryExtension:variantFilter
[Maven-Publish plugin]: https://docs.gradle.org/current/userguide/publishing_maven.html
[default publishing configuration on the AGP DSL]: https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.LibraryExtension.html#com.android.build.gradle.LibraryExtension:defaultPublishConfig

[AndroidComponentsExtension]: plugin/src/main/kotlin/com/trevjonez/AndroidComponentsExtension.kt

## License

    Copyright 2018 Trevor Jones

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.