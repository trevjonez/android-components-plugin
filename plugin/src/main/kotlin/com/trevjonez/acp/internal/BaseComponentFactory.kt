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

package com.trevjonez.acp.internal

import com.android.build.gradle.api.BaseVariant
import com.trevjonez.acp.AndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.provider.Provider

internal abstract class BaseComponentFactory<C : AndroidVariantComponent, V : BaseVariant>(
    val project: Project,
    val attributesFactory: ImmutableAttributesFactory
) {
  val componentsExtension =
      project.extensions.findByType(AndroidComponentsExtension::class.java)
          ?: throw NullPointerException("Do not apply ${javaClass.simpleName} directly. Use \"android-components\"")

  val groupProvider: Provider<String> = project.provider {
    project.group.toString()
  }

  val baseNameProvider: Provider<String> = project.provider {
    componentsExtension.artifactId ?: project.name
  }

  val versionProvider: Provider<String> = project.provider {
    project.version.toString()
  }

  fun configuration(configName: String): Configuration =
      project.configurations.getByName(configName)

  fun usage(name: String): Usage = project.objects.named(Usage::class.java, name)

  fun moduleVersionIdentifier(nameSuffix: String = ""): ModuleVersionIdentifier {
    return DefaultModuleVersionIdentifier.newId(
        groupProvider.get(),
        baseNameProvider.get() + nameSuffix,
        versionProvider.get())
  }

  abstract val defaultConfigProvider: Provider<String>
  abstract fun rootComponent(): AndroidComponent<C>
  abstract fun variantComponent(variant: V): C
}

