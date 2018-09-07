# Android-Components-Plugin

Variant aware artifact publishing for android projects.

## Preface

Gradle and the [Android Gradle Plugin(s)] (AGP) provide/consume dependencies 
in a variant aware way when depending on projects directly. The Maven [POM format] 
does not support exposing gradle metadata to achieve the same level of itegration
when consuming a published artifact.

The goal of this plugin is to provide support for the 
[Gradle module metadata specification] as well as reduce the amount of work 
required to setup publishing android projects. 

## Installation and Configuration

The plugin is available via the [Gradle Plugin Portal]

```kotlin
//build.gradle.kts
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

`android-components` binds the configuration of the applied AGP into the 
[Maven-Publish plugin]. This is done by creating a [SoftwareComponent] instance 
for each variant which is registered as part of a single [ComponentWithVariants]
that represents the entire module. By implementing [ComponentWithVariants] gradle
will automatically produce the appropriate .module metadata file when attached to
a publication.

The published artifact coordinates the project group, name, and version. 

The primary artifact id can be overridden via the [AndroidComponentsExtension]
Project naming should be preferred over using this override.
```kotlin
//build.gradle(.kts)
androidComponents {
  artifactId = "CustomId"
}
```

Android Gradle Plugin Behaviors

| AGP Plugin ID                | Published Artifacts      |
|------------------------------|--------------------------|
|`com.android.library`         | AAR, Sources JAR         |
|`com.android.application`     | See [Issue #2]           |
|`com.android.test`            | See [Issue #3]           |
|`com.android.feature`         | See [Issue #4]           |
|`com.android.instantapp`      | See [Issue #5]           |  
|`com.android.dynamic-feature` | See [Issue #8]           |  

## Artifact Consumption
#### .module metadata

In order to consume the [.module][Gradle module metadata specification] metadata
in downstream projects `GRADLE_METADATA` must be enabled via the 
[Settings.enableFeaturePreview] method.

```kotlin
//settings.gradle(.kts)
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
[default publishing configuration on the AGP DSL].

```kotlin
dependencies {
  //POM artifact that depends on "com.example:lib_release:1.2.3"
  implementation("com.example:lib:1.2.3")
}
```

or manually select variants per configuration:

```kotlin
dependencies {
  debugImplementation("com.example:lib_debug:1.2.3")
  releaseImplementation("com.example:lib_release:1.2.3")
}
```

#### AGP/Gradle Compatibility

The following version combinations have been tested to work:

| AGP Version    | Gradle Version | Android Components Version |
|----------------|----------------|----------------------------|
| 3.1.4          | 4.10           | 0.1.0                      |
| 3.2.0-rc02     | 4.10           | 0.1.0                      |
| 3.3.0-alpha08  | 4.10           | 0.1.0                      |


#### Troubleshooting

If you are met with issues surrounding variant mismatching review the AGP DSL
documentation for [defaultConfig.missingDimensionStrategy] as the same rules apply.

At that point if you are still unable to resolve the problem feel free to 
[open a new issue] to discuss further. 

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
    
[POM format]: https://maven.apache.org/pom.html
[Gradle module metadata specification]: https://github.com/gradle/gradle/blob/master/subprojects/docs/src/docs/design/gradle-module-metadata-specification.md
[SoftwareComponent]: https://docs.gradle.org/current/javadoc/org/gradle/api/component/SoftwareComponent.html
[ComponentWithVariants]: https://docs.gradle.org/current/javadoc/org/gradle/api/component/ComponentWithVariants.html
[Gradle Plugin Portal]: https://plugins.gradle.org/
[Android Gradle Plugin(s)]: https://developer.android.com/studio/releases/gradle-plugin
[Maven-Publish plugin]: https://docs.gradle.org/current/userguide/publishing_maven.html
[default publishing configuration on the AGP DSL]: https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.LibraryExtension.html#com.android.build.gradle.LibraryExtension:defaultPublishConfig
[defaultConfig.missingDimensionStrategy]: https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.DefaultConfig.html#com.android.build.gradle.internal.dsl.DefaultConfig:missingDimensionStrategy(java.lang.String,%20java.lang.String)
[open a new issue]: https://github.com/trevjonez/android-components-plugin/issues/new
[Settings.enableFeaturePreview]: https://docs.gradle.org/current/javadoc/org/gradle/api/initialization/Settings.html#enableFeaturePreview-java.lang.String-
[AndroidComponentsExtension]: plugin/src/main/kotlin/com/trevjonez/acp/AndroidComponentsExtension.kt

[Issue #2]: https://github.com/trevjonez/android-components-plugin/issues/2
[Issue #3]: https://github.com/trevjonez/android-components-plugin/issues/3
[Issue #4]: https://github.com/trevjonez/android-components-plugin/issues/4
[Issue #5]: https://github.com/trevjonez/android-components-plugin/issues/5
[Issue #8]: https://github.com/trevjonez/android-components-plugin/issues/8