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

package com.trevjonez.acp.internal.plugins

import com.android.build.gradle.LibraryExtension
import com.trevjonez.acp.internal.library.LibraryComponentFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

internal class AndroidLibraryComponentsPlugin
@Inject constructor(
    private val attributesFactory: ImmutableAttributesFactory
) : Plugin<Project> {

  private lateinit var project: Project

  private val libExtension by lazy(NONE) {
    project.extensions.findByType(LibraryExtension::class.java)
        ?: throw NullPointerException("Do not apply ${javaClass.simpleName} directly.")
  }

  private val componentFactory by lazy(NONE) {
    LibraryComponentFactory(project, attributesFactory, libExtension)
  }

  private val rootComponent by lazy(NONE) {
    componentFactory.rootComponent()
  }

  override fun apply(target: Project) {
    project = target

    libExtension.libraryVariants.all {
      val variantComponent = componentFactory.variantComponent(this)
      rootComponent.variantComponents.add(variantComponent)
    }

    project.components.add(rootComponent)
    project.components.addAll(rootComponent.variantComponents)

    registerComponentsWithMavenPublishPlugin()
  }

  private fun registerComponentsWithMavenPublishPlugin() {
    project.pluginManager.withPlugin("maven-publish") {
      project.extensions.configure(PublishingExtension::class.java) {
        publications {
          register("android", MavenPublication::class.java) {
            this as MavenPublicationInternal
            mavenProjectIdentity.artifactId.set(
                componentFactory.baseNameProvider
            )
            from(rootComponent)
          }

          rootComponent.variantComponents.all {
            val variantComponent = this
            register(name, MavenPublication::class.java) {
              (this as MavenPublicationInternal)

              groupId = variantComponent.coordinates.group
              artifactId = variantComponent.coordinates.name
              version = variantComponent.coordinates.version

              from(variantComponent)
              publishWithOriginalFileName()

              //TODO can this be implicit to variantComponent?
              artifact(LazyPublishArtifact(sourcesTask))
            }
          }
        }
      }
    }
  }
}
