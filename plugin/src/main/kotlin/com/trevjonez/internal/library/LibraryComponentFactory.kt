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

package com.trevjonez.internal.library

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import com.trevjonez.internal.AndroidComponent
import com.trevjonez.internal.BaseComponentFactory
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

class LibraryComponentFactory(
    project: Project,
    attributesFactory: ImmutableAttributesFactory,
    libraryExtension: LibraryExtension
) : BaseComponentFactory<LibraryVariantComponent, LibraryVariant>(project, attributesFactory) {

  override val defaultConfigProvider: Provider<String> = project.provider {
    libraryExtension.defaultPublishConfig
  }

  override fun rootComponent(): AndroidComponent<LibraryVariantComponent> {
    return AndroidComponent(DefaultDomainObjectSet(LibraryVariantComponent::class.java), this)
  }

  override fun variantComponent(variant: LibraryVariant): LibraryVariantComponent {
    val sourcesTask = libSourceTask(variant)

    return LibraryVariantComponent(variant, this, sourcesTask)
  }

  private fun libSourceTask(variant: LibraryVariant): TaskProvider<Jar> {
    return project.tasks.register(
        "${variant.name}SourcesJar",
        Jar::class.java
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
  }
}