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

package com.trevjonez.acp.internal.library

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import com.trevjonez.acp.internal.AndroidComponent
import com.trevjonez.acp.internal.BaseComponentFactory
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

internal class LibraryComponentFactory(
    project: Project,
    attributesFactory: ImmutableAttributesFactory,
    val libraryExtension: LibraryExtension
) : BaseComponentFactory<LibraryVariantComponent, LibraryVariant>(project, attributesFactory) {

  override val defaultConfigProvider: Provider<String> = project.provider {
    libraryExtension.defaultPublishConfig
  }

  override fun rootComponent(): AndroidComponent<LibraryVariantComponent> {
    return AndroidComponent(DefaultDomainObjectSet(LibraryVariantComponent::class.java), this)
  }

  override fun variantComponent(variant: LibraryVariant): LibraryVariantComponent {
    return LibraryVariantComponent(
        variant,
        this,
        libSourceTask(variant))
  }

  private fun libSourceTask(variant: LibraryVariant): TaskProvider<Jar> {
    return project.tasks.register(
        "${variant.name}SourcesJar",
        Jar::class.java
    ) {
      classifier = "sources"
      variant.sourceSets.forEach { sourceProvider ->
        from(sourceProvider.aidlDirectories) { into("aidl") }
        from(sourceProvider.cDirectories) { into("c") }
        from(sourceProvider.cppDirectories) { into("cpp") }
        from(sourceProvider.javaDirectories) { into("java") }
        from(sourceProvider.renderscriptDirectories) { into("renderscript") }
        from(sourceProvider.shadersDirectories) { into("shaders") }
      }
    }
  }
}