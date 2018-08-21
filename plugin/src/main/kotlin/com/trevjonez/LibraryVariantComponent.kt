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
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.attributes.Usage.*
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.plugins.JavaPlugin.*
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.internal.resolve.LibraryPublishArtifact
import org.gradle.jvm.tasks.Jar

class LibraryVariantComponent(
    override val variant: LibraryVariant,
    override val baseComps: BaseComponentProvider,
    val sources: TaskProvider<Jar>
) : AndroidVariantComponent {

  override fun getName(): String =
      variant.name

  override fun getOutputs(): FileCollection =
      variant.packageLibrary.outputs.files

  override fun getCoordinates(): ModuleVersionIdentifier {
    return baseComps.moduleVersionIdentifier("_${variant.combinedNames}")
  }

  override fun getUsages(): Set<UsageContext> {
    return setOf(
        usageFor(
            "api", API_ELEMENTS_CONFIGURATION_NAME,
            JAVA_API, variant.compileConfiguration
        ),
        usageFor(
            "runtime", RUNTIME_ELEMENTS_CONFIGURATION_NAME,
            JAVA_RUNTIME, variant.runtimeConfiguration
        )
    )
  }

  private fun usageFor(
      configType: String, configName: String,
      usageName: String, configuration: Configuration
  ) = AndroidVariantUsage(
      variant.name,
      configType,
      baseComps.configuration("${variant.name}${configName.capitalize()}"),
      baseComps.provider { _ ->
        baseComps.attributesFactory.mutable()
            .attribute(USAGE_ATTRIBUTE, baseComps.usage(usageName))
            .addAll(configuration.attributes)
      },
      baseComps.provider { _ ->
        variant.outputs
            .map {
              LibraryPublishArtifact(it.outputType, it.outputFile)
                  .builtBy(it.assemble)
            }
            .toSet()
      }
  )

  private val LibraryVariant.combinedNames: String
    get() {
      return (productFlavors + buildType)
          .joinToString(separator = "_") { it.name }
    }
}