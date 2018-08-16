package com.trevjonez

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
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
import org.gradle.api.internal.artifacts.dependencies.DefaultClientModule
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.plugins.JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
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
      val libExtension =
          project.extensions.findByType(LibraryExtension::class.java)!!

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
}

interface AndroidVariantComponent :
    ComponentWithOutputs,
    PublishableComponent,
    SoftwareComponentInternal {
  val variant: BaseVariant
  val baseComps: BaseComponentProvider
}

class LibraryVariantComponent(
    private val project: Project,
    private val attributesFactory: ImmutableAttributesFactory,
    override val variant: LibraryVariant,
    override val baseComps: BaseComponentProvider
) : AndroidVariantComponent {
  override fun getName(): String =
      variant.name

  override fun getOutputs(): FileCollection =
      variant.packageLibrary.outputs.files

  override fun getCoordinates(): ModuleVersionIdentifier {
    return baseComps.moduleVersionIdentifier("_${variant.combinedNames}")
  }

  override fun getUsages(): Set<UsageContext> {

    //TODO clean up this function
    val apiElements = "${variant.name}${API_ELEMENTS_CONFIGURATION_NAME.capitalize()}"
    val runtimeElements = "${variant.name}${RUNTIME_ELEMENTS_CONFIGURATION_NAME.capitalize()}"
    return setOf(
        AndroidVariantUsage(
            variant.name, "api",
            project.configurations.getByName(apiElements),
            project.provider {
              attributesFactory.mutable()
                  .attribute(
                      Usage.USAGE_ATTRIBUTE,
                      project.objects.named(Usage::class.java, Usage.JAVA_API))
                  .addAll(variant.compileConfiguration.attributes)
            },
            project.provider {
              variant.outputs
                  .map {
                    LibraryPublishArtifact(it.outputType, it.outputFile)
                        .builtBy(it.assemble)
                  }
                  .toSet()
            }
        ),
        AndroidVariantUsage(
            variant.name, "runtime",
            project.configurations.getByName(runtimeElements),
            project.provider {
              attributesFactory.mutable()
                  .attribute(
                      Usage.USAGE_ATTRIBUTE,
                      project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
                  .addAll(variant.runtimeConfiguration.attributes)
            },
            project.provider {
              variant.outputs
                  .map {
                    LibraryPublishArtifact(it.outputType, it.outputFile)
                        .builtBy(it.assemble)
                  }
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

class BaseComponentProvider(
    val defaultConfigProvider: Provider<String>,
    val groupProvider: Provider<String>,
    val baseNameProvider: Provider<String>,
    val versionProvider: Provider<String>
) {
  fun moduleVersionIdentifier(nameSuffix: String = ""): ModuleVersionIdentifier {
    return DefaultModuleVersionIdentifier.newId(
        groupProvider.get(),
        baseNameProvider.get() + nameSuffix,
        versionProvider.get())
  }
}

class AndroidComponent<VC : AndroidVariantComponent>(
    private val project: Project,
    private val attributesFactory: ImmutableAttributesFactory,
    val variantComponents: DomainObjectSet<VC>,
    override val baseComps: BaseComponentProvider
) : ComponentWithVariants, AndroidVariantComponent {
  override val variant: BaseVariant
    get() = defaultVariant.variant

  private val defaultVariant: VC by lazy {
    val defaultName = baseComps.defaultConfigProvider.get()
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
          project.provider {
            attributesFactory.of(
                Usage.USAGE_ATTRIBUTE,
                project.objects.named(Usage::class.java, Usage.JAVA_API)
            )
          }
      ))

  override fun getOutputs(): FileCollection = project.files()

  override fun getCoordinates(): ModuleVersionIdentifier {
    return baseComps.moduleVersionIdentifier()
  }
}

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

inline infix fun <reified T> Attribute<T>.from(container: AttributeContainer): T =
    T::class.java.cast(container.getAttribute(this))
