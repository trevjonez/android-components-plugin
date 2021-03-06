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
import com.trevjonez.acp.internal.usage.DefaultVariantForwardingUsage
import org.gradle.api.DomainObjectSet
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.attributes.Usage
import org.gradle.api.component.ComponentWithVariants
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.artifacts.dependencies.DefaultClientModule
import org.gradle.api.internal.component.UsageContext
import kotlin.LazyThreadSafetyMode.*

internal class AndroidComponent<VC : AndroidVariantComponent>(
    val variantComponents: DomainObjectSet<VC>,
    override val compFactory: BaseComponentFactory<VC, *>
) : ComponentWithVariants, AndroidVariantComponent {
  override val variant: BaseVariant
    get() = defaultVariant.variant

  private val defaultVariant: VC by lazy(NONE) {
    val defaultName = compFactory.defaultConfigProvider.get()
    variantComponents.firstOrNull { it.variant.name == defaultName }
        ?: throw IllegalStateException(
            """Specified default publish config `$defaultName` was not found.
          |To correct this issue update your android DSL block with one of:
          |${variantComponents.joinToString("\n", "\n", "\n\n") {
              "  - \"${it.variant.name}\""
            }}
          |```
          |android {
          |  defaultPublishConfig = "${variantComponents.first().variant.name}"
          |  [...]
          |}
          |```
          |""".trimMargin())
  }

  override fun getName() = "android"

  override fun getVariants(): Set<SoftwareComponent> =
      variantComponents

  override fun getUsages(): Set<UsageContext> =
      setOf(DefaultVariantForwardingUsage(
          DefaultClientModule(
              defaultVariant.coordinates.group,
              defaultVariant.coordinates.name,
              defaultVariant.coordinates.version
          ),
          compFactory.project.provider {
            compFactory.attributesFactory.of(
                Usage.USAGE_ATTRIBUTE,
                compFactory.project.objects.named(Usage::class.java, Usage.JAVA_API)
            )
          }
      ))

  override fun getOutputs(): FileCollection = compFactory.project.files()

  override fun getCoordinates(): ModuleVersionIdentifier {
    return compFactory.moduleVersionIdentifier()
  }
}