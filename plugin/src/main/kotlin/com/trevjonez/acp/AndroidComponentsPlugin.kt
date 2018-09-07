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

package com.trevjonez.acp

import com.android.build.gradle.api.AndroidBasePlugin
import com.trevjonez.acp.internal.plugins.AndroidLibraryComponentsPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findPlugin
import kotlin.reflect.jvm.jvmName

class AndroidComponentsPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.pluginManager.withPlugin("com.android.library") {
      addExtension(project)
      project.pluginManager.apply(AndroidLibraryComponentsPlugin::class.java)
    }
    project.afterEvaluate {
      extensions.findByType(AndroidComponentsExtension::class.java)
          ?: missingPlugin<AndroidBasePlugin>(
              githubIssueMessage() ?: genericExceptionMessage
          )

      plugins.findPlugin(MavenPublishPlugin::class)
          ?: missingPlugin<MavenPublishPlugin>(
              """maven-publish is currently the only supported publishing plugin.
                |$genericExceptionMessage""".trimMargin())
    }
  }

  private fun addExtension(project: Project) {
    project.extensions.create<AndroidComponentsExtension>("androidComponents")
  }

  private inline fun <reified T : Plugin<Project>> Project.missingPlugin(msg: String = ""): Nothing {
    throw MissingPluginException(
        """Failed to configure ${AndroidComponentsPlugin::class.jvmName} plugin on project: $path
          |  Expected plugin: `${T::class.java.name}` was not applied.
          |  $msg""".trimMargin()
    )
  }

  private fun Project.githubIssueMessage() = AGP_PLUGIN_ISSUES
      .filter { (pluginId, _) ->
        plugins.hasPlugin(pluginId)
      }
      .takeIf {
        it.isNotEmpty()
      }
      ?.joinToString(
          prefix = "The follow android plugins were found but are not yet supported:\n",
          separator = "\n\n"
      ) { (pluginId, issueNumber) ->
        """$pluginId
          |  Github Issue: $ACP_ISSUES$issueNumber""".trimMargin()
      }

  private val genericExceptionMessage =
      """If you believe this is an issue or missing feature, please consider opening an issue on github.
        |$ACP_NEW_ISSUE
        |""".trimMargin()

  private companion object {
    private const val ACP_ISSUES = "https://github.com/trevjonez/android-components-plugin/issues/"
    private const val ACP_NEW_ISSUE = ACP_ISSUES + "new"
    private val AGP_PLUGIN_ISSUES = listOf(
        "com.android.application" to 2,
        "com.android.test" to 3,
        "com.android.library" to 4,
        "com.android.instantapp" to 5,
        "com.android.dynamic-feature" to 8
    )
  }

  class MissingPluginException(message: String) : GradleException(message)
}
