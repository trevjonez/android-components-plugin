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

import com.android.build.gradle.api.LibraryVariant
import com.trevjonez.acp.internal.AndroidVariantComponent
import com.trevjonez.acp.internal.addAll
import com.trevjonez.acp.internal.usage.AndroidVariantUsage
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.attributes.Usage.*
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.plugins.JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.internal.resolve.LibraryPublishArtifact

internal class LibraryVariantComponent(
    override val variant: LibraryVariant,
    override val compFactory: LibraryComponentFactory,
    val sourcesTask: TaskProvider<out AbstractArchiveTask>
) : AndroidVariantComponent {

  override fun getName(): String =
      variant.name

  override fun getOutputs(): FileCollection =
      variant.packageLibrary.outputs.files

  override fun getCoordinates(): ModuleVersionIdentifier {
    return compFactory.moduleVersionIdentifier("_${variant.combinedNames}")
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
      compFactory.configuration("${variant.name}${configName.capitalize()}"),
      compFactory.project.provider {
        compFactory.attributesFactory.mutable()
            .attribute(USAGE_ATTRIBUTE, compFactory.usage(usageName))
            .addAll(configuration.attributes)
      },
      compFactory.project.provider {
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