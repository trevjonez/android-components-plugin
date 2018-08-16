package com.trevjonez

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Usage
import org.gradle.api.capabilities.Capability
import org.gradle.api.component.ComponentWithVariants
import org.gradle.api.component.PublishableComponent
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.artifacts.configurations.Configurations
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin.*
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.gradle.jvm.internal.resolve.LibraryPublishArtifact
import org.gradle.language.ComponentWithOutputs
import javax.inject.Inject

@Suppress("unused")
class AndroidComponentsPlugin
@Inject constructor(
    private val attributesFactory: ImmutableAttributesFactory
) : Plugin<Project> {

  override fun apply(project: Project) {
    //TODO only libraries for now. if all goes well we will cover the other types.
    project.pluginManager.withPlugin("com.android.library") { _ ->
      val libExtension = project.extensions.findByType(LibraryExtension::class.java)!!

      val defaultConfig =
          project.provider { libExtension.defaultPublishConfig }

      val group: Provider<String> =
          project.provider { project.group.toString() }

      val baseName: Provider<String> =
          project.provider { project.name }

      val versionP: Provider<String> =
          project.provider { project.version.toString() }

      val rootComponent = AndroidComponent(
          project.logger, defaultConfig, group, baseName, versionP,
          DefaultDomainObjectSet(LibraryVariantComponent::class.java)
      )

      libExtension.libraryVariants.all { variant ->
        project.components.add(rootComponent)
        val variantComponent = LibraryVariantComponent(
            project.providers, project.objects,
            attributesFactory, project.configurations,
            variant, group, baseName, versionP
        )

        project.components.add(variantComponent)
        rootComponent.variantComponents.add(variantComponent)
      }

      project.pluginManager.withPlugin("maven-publish") { _ ->
        project.extensions.configure("publishing") { publishing: PublishingExtension ->
          publishing.publications.apply {
            maybeCreate("android", MavenPublication::class.java).apply {
              this as MavenPublicationInternal
              mavenProjectIdentity.artifactId.set(baseName)
              from(rootComponent)
              publishWithOriginalFileName()
            }
            rootComponent.variantComponents.all(object : Action<LibraryVariantComponent> {
              override fun execute(variantComponent: LibraryVariantComponent) {
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
            })
          }
        }
      }
    }
  }
}

interface AndroidVariantComponent :
    ComponentWithOutputs,
    PublishableComponent,
    SoftwareComponentInternal {
  val variant: BaseVariant
  val group: Provider<String>
  val baseName: Provider<String>
  val version: Provider<String>
}

class LibraryVariantComponent(
    private val providers: ProviderFactory,
    private val objects: ObjectFactory,
    private val attributesFactory: ImmutableAttributesFactory,
    private val configurationContainer: ConfigurationContainer,
    override val variant: LibraryVariant,
    override val group: Provider<String>,
    override val baseName: Provider<String>,
    override val version: Provider<String>
) : AndroidVariantComponent {
  override fun getName(): String =
      variant.name

  override fun getOutputs(): FileCollection =
      variant.packageLibrary.outputs.files

  override fun getCoordinates(): ModuleVersionIdentifier {
    return DefaultModuleVersionIdentifier.newId(
        group.get(),
        "${baseName.get()}_${variant.combinedNames}",
        version.get()
    )
  }

  override fun getUsages(): Set<UsageContext> {

    val apiElements = "${variant.name}${API_ELEMENTS_CONFIGURATION_NAME.capitalize()}"
    val runtimeElements = "${variant.name}${RUNTIME_ELEMENTS_CONFIGURATION_NAME.capitalize()}"
    return setOf(
        AndroidVariantUsage(
            variant.name,
            "api",
            configurationContainer.getByName(apiElements),

            providers.provider {
              attributesFactory.mutable()
                  .attribute(
                      Usage.USAGE_ATTRIBUTE,
                      objects.named(Usage::class.java, Usage.JAVA_API))
                  .addAll(variant.compileConfiguration.attributes)
            },

            providers.provider {
              variant.outputs
                  .map { LibraryPublishArtifact(it.outputType, it.outputFile).builtBy(it.assemble) }
                  .toSet()
            }
        ),
        AndroidVariantUsage(
            variant.name,
            "runtime",
            configurationContainer.getByName(runtimeElements),

            providers.provider {
              attributesFactory.mutable()
                  .attribute(
                      Usage.USAGE_ATTRIBUTE,
                      objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
                  .addAll(variant.runtimeConfiguration.attributes)
            },

            providers.provider {
              variant.outputs
                  .map { LibraryPublishArtifact(it.outputType, it.outputFile).builtBy(it.assemble) }
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

@Suppress("UNCHECKED_CAST")
private fun AttributeContainer.addAll(
    attributes: AttributeContainer
): AttributeContainer = apply {
  attributes.keySet().forEach {
    it as Attribute<Any>
    attribute(it, attributes.getAttribute(it)!!)
  }
}

class AndroidComponent<VC : AndroidVariantComponent>(
    private val logger: Logger,
    private val defaultPublishConfig: Provider<String>,
    override val group: Provider<String>,
    override val baseName: Provider<String>,
    override val version: Provider<String>,
    val variantComponents: DomainObjectSet<VC>
) : ComponentWithVariants, AndroidVariantComponent {
  override val variant: BaseVariant
    get() = defaultVariant.variant

  private val defaultVariant: VC
    get() {
      val defaultName = defaultPublishConfig.get()
      val default = variantComponents.singleOrNull { it.name == defaultName }
      if (default != null) {
        return default
      } else {
        val fallback = variantComponents.firstOrNull { it.variant.buildType.name == defaultName }
        if (fallback == null) throw IllegalStateException("defaultPublishConfig: `$defaultName` not found.")
        else {
          logger.warn("defaultPublishConfig `$defaultName` not found. auto selecting ${fallback.name}")
          return fallback
        }
      }
    }

  override fun getName() = "android"

  override fun getVariants(): Set<SoftwareComponent> =
      variantComponents

  override fun getUsages(): Set<UsageContext> =
      defaultVariant.usages

  override fun getOutputs(): FileCollection =
      defaultVariant.outputs

  override fun getCoordinates(): ModuleVersionIdentifier {
    return DefaultModuleVersionIdentifier.newId(
        group.get(), baseName.get(), version.get()
    )
  }
}

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

inline infix fun <reified T> Attribute<T>.from(container: AttributeContainer): T =
    T::class.java.cast(container.getAttribute(this))
