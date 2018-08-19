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

import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Usage
import org.gradle.api.capabilities.Capability
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.provider.Provider

class DefaultVariantForwardingUsage(
    private val redirectDependency: ModuleDependency,
    private val attributes: Provider<AttributeContainer>
) : UsageContext {
  override fun getUsage(): Usage =
      Usage.USAGE_ATTRIBUTE from attributes.get()

  override fun getName(): String {
    return "legacy-support-redirect"
  }

  override fun getCapabilities(): Set<Capability> =
      emptySet()

  override fun getDependencies(): Set<ModuleDependency> =
      setOf(redirectDependency)

  override fun getDependencyConstraints(): Set<DependencyConstraint> =
      emptySet()

  override fun getGlobalExcludes(): Set<ExcludeRule> =
      emptySet()

  override fun getArtifacts(): Set<PublishArtifact> =
      emptySet()

  override fun getAttributes(): AttributeContainer =
      attributes.get()
}