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

package com.trevjonez

import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

class BaseComponentProvider(
    val project: Project,

    val attributesFactory: ImmutableAttributesFactory,

    val componentsExtension: AndroidComponentsExtension,

    val defaultConfigProvider: Provider<String>,

    val groupProvider: Provider<String> =
        project.provider { project.group.toString() },

    val baseNameProvider: Provider<String> =
        project.provider { componentsExtension.artifactId ?: project.name },

    val versionProvider: Provider<String> =
        project.provider { project.version.toString() }
) {

  inline fun <T> provider(crossinline block: (project: Project) -> T) =
      project.provider { block(project) }

  fun configuration(configName: String) =
      project.configurations.getByName(configName)

  fun usage(name: String) = project.objects.named(Usage::class.java, name)

  fun moduleVersionIdentifier(nameSuffix: String = ""): ModuleVersionIdentifier {
    return DefaultModuleVersionIdentifier.newId(
        groupProvider.get(),
        baseNameProvider.get() + nameSuffix,
        versionProvider.get())
  }

  inline fun <reified T : AndroidVariantComponent> rootComponent() =
      AndroidComponent<T>(DefaultDomainObjectSet(T::class.java), this)

  fun libVariantComponent(variant: LibraryVariant): LibraryVariantComponent {
    val sourcesTask = project.tasks.register(
        "${variant.name}SourcesJar", Jar::class.java
    ) { jarTask ->
      jarTask.apply {
        classifier = "sources"
        onlyIf { _ -> !componentsExtension.disableSourcePublishing }
        variant.sourceSets.forEach { sourceProvider ->
          from(sourceProvider.aidlDirectories) { spec ->
            spec.into("aidl")
          }
          from(sourceProvider.cDirectories) { spec ->
            spec.into("c")
          }
          from(sourceProvider.cppDirectories) { spec ->
            spec.into("cpp")
          }
          from(sourceProvider.javaDirectories) { spec ->
            spec.into("java")
          }
          from(sourceProvider.renderscriptDirectories) { spec ->
            spec.into("renderscript")
          }
          from(sourceProvider.shadersDirectories) { spec ->
            spec.into("shaders")
          }
        }
      }
    }
    return LibraryVariantComponent(variant, this, sourcesTask)
  }
}