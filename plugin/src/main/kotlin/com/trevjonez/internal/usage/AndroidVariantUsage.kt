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

package com.trevjonez.internal.usage

import com.trevjonez.internal.from
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Usage
import org.gradle.api.capabilities.Capability
import org.gradle.api.internal.artifacts.configurations.Configurations
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.provider.Provider

class AndroidVariantUsage(
    private val variantName: String,
    private val configType: String,
    private val config: Configuration,
    private val attributes: Provider<AttributeContainer>,
    private val artifacts: Provider<Set<PublishArtifact>>
) : UsageContext {

  override fun getName() =
      "$variantName${configType.capitalize()}"

  override fun getArtifacts(): Set<PublishArtifact> =
      artifacts.get()

  //TODO auto convert project into artifact?
  override fun getDependencies(): Set<ModuleDependency> =
      config.incoming.dependencies.withType(ModuleDependency::class.java)

  override fun getDependencyConstraints(): Set<DependencyConstraint> =
      config.incoming.dependencyConstraints

  override fun getCapabilities(): Set<Capability> =
      Configurations.collectCapabilities(config, mutableSetOf(), mutableSetOf())

  override fun getGlobalExcludes(): Set<ExcludeRule> =
      config.excludeRules

  override fun getUsage(): Usage =
      Usage.USAGE_ATTRIBUTE from attributes.get()

  override fun getAttributes(): AttributeContainer =
      attributes.get()
}