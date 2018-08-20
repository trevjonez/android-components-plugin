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

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import javax.inject.Inject
import kotlin.NullPointerException

class AndroidLibraryComponentsPlugin
@Inject constructor(
    private val attributesFactory: ImmutableAttributesFactory
) : Plugin<Project> {

  override fun apply(project: Project) {
    val libExtension =
        project.extensions.findByType(LibraryExtension::class.java)
            ?: throw NullPointerException("Do not apply ${javaClass.simpleName} directly. Use \"android-components\"")

    val componentsExtension =
        project.extensions.findByType(AndroidComponentsExtension::class.java)
            ?: throw NullPointerException("Do not apply ${javaClass.simpleName} directly. Use \"android-components\"")

    val baseComponentProvider = BaseComponentProvider(
        project,
        attributesFactory,
        componentsExtension,
        project.provider {
          libExtension.defaultPublishConfig
        })

    val rootComponent = baseComponentProvider.rootComponent<LibraryVariantComponent>()

    project.components.add(rootComponent)

    libExtension.libraryVariants.all { variant ->
      val variantComponent = baseComponentProvider.libVariantComponent(variant)
      project.components.add(variantComponent)
      rootComponent.variantComponents.add(variantComponent)
    }

    registerComponentsWithMavenPublishPlugin(
        project, baseComponentProvider, rootComponent
    )
  }

  private fun registerComponentsWithMavenPublishPlugin(
      project: Project,
      baseComponentProvider: BaseComponentProvider,
      rootComponent: AndroidComponent<LibraryVariantComponent>
  ) {
    project.pluginManager.withPlugin("maven-publish") { _ ->
      project.extensions.configure("publishing") { publishing: PublishingExtension ->
        publishing.publications.apply {
          val androidExists = findByName("android") != null
          maybeCreate("android", MavenPublication::class.java).apply {
            this as MavenPublicationInternal
            if (!androidExists) {
              mavenProjectIdentity.artifactId.set(
                  baseComponentProvider.baseNameProvider
              )
            }
            from(rootComponent)
          }
          rootComponent.variantComponents.all { variantComponent ->
            maybeCreate(variantComponent.name, MavenPublication::class.java).apply {
              this as MavenPublicationInternal
              mavenProjectIdentity.apply {
                groupId.set(project.provider {
                  variantComponent.coordinates.group
                })
                artifactId.set(project.provider {
                  variantComponent.coordinates.name
                })
                version.set(project.provider {
                  variantComponent.coordinates.version
                })
              }
              from(variantComponent)
              publishWithOriginalFileName()
            }
          }
        }
      }
    }
  }
}