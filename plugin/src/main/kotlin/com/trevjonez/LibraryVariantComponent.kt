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
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.internal.resolve.LibraryPublishArtifact

class LibraryVariantComponent(
    private val project: Project,
    private val attributesFactory: ImmutableAttributesFactory,
    override val variant: LibraryVariant,
    override val baseComps: BaseComponentProvider
) : AndroidVariantComponent {
  override fun getName(): String =
      variant.name

  override fun getOutputs(): FileCollection =
      variant.packageLibrary.outputs.files

  override fun getCoordinates(): ModuleVersionIdentifier {
    return baseComps.moduleVersionIdentifier("_${variant.combinedNames}")
  }

  override fun getUsages(): Set<UsageContext> {

    //TODO clean up this function
    val apiElements = "${variant.name}${JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME.capitalize()}"
    val runtimeElements = "${variant.name}${JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME.capitalize()}"
    return setOf(
        AndroidVariantUsage(
            variant.name, "api",
            project.configurations.getByName(apiElements),
            project.provider {
              attributesFactory.mutable()
                  .attribute(
                      Usage.USAGE_ATTRIBUTE,
                      project.objects.named(Usage::class.java, Usage.JAVA_API))
                  .addAll(variant.compileConfiguration.attributes)
            },
            project.provider {
              variant.outputs
                  .map {
                    LibraryPublishArtifact(it.outputType, it.outputFile)
                        .builtBy(it.assemble)
                  }
                  .toSet()
            }
        ),
        AndroidVariantUsage(
            variant.name, "runtime",
            project.configurations.getByName(runtimeElements),
            project.provider {
              attributesFactory.mutable()
                  .attribute(
                      Usage.USAGE_ATTRIBUTE,
                      project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
                  .addAll(variant.runtimeConfiguration.attributes)
            },
            project.provider {
              variant.outputs
                  .map {
                    LibraryPublishArtifact(it.outputType, it.outputFile)
                        .builtBy(it.assemble)
                  }
                  .toSet()
            }
        )
    )
  }

  private val LibraryVariant.combinedNames: String
    get() {
      return (productFlavors + buildType).joinToString(separator = "_") { it.name }
    }
}