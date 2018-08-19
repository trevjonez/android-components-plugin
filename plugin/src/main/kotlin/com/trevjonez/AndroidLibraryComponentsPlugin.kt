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
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import java.lang.NullPointerException
import javax.inject.Inject

@Suppress("unused")
class AndroidLibraryComponentsPlugin
@Inject constructor(
    private val attributesFactory: ImmutableAttributesFactory
) : Plugin<Project> {

  override fun apply(project: Project) {
    val libExtension =
        project.extensions.findByType(LibraryExtension::class.java)
            ?: throw NullPointerException("Do not apply ${javaClass.simpleName} directly. Use \"android-components\"")

    val baseComponentProvider = BaseComponentProvider(
        project.provider { libExtension.defaultPublishConfig },
        project.provider { project.group.toString() },
        project.provider { project.name },
        project.provider { project.version.toString() }
    )

    val rootComponent: AndroidComponent<LibraryVariantComponent> =
        AndroidComponent(
            project, attributesFactory,
            DefaultDomainObjectSet(LibraryVariantComponent::class.java),
            baseComponentProvider
        )

    libExtension.libraryVariants.all { variant ->
      project.components.add(rootComponent)
      val variantComponent = LibraryVariantComponent(
          project, attributesFactory, variant, baseComponentProvider
      )

      project.components.add(variantComponent)
      rootComponent.variantComponents.add(variantComponent)
    }

    project.pluginManager.withPlugin("maven-publish") { _ ->
      project.extensions.configure("publishing") { publishing: PublishingExtension ->
        publishing.publications.apply {
          maybeCreate("android", MavenPublication::class.java).apply {
            this as MavenPublicationInternal
            mavenProjectIdentity.artifactId.set(baseComponentProvider.baseNameProvider)
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