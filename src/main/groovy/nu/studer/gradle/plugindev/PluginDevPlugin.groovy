/*
 * Copyright 2014 Etienne Studer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nu.studer.gradle.plugindev

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import nu.studer.gradle.util.Licenses
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.ClasspathNormalizer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Plugin for Gradle plugin development. The PluginDevPlugin creates a MavenPublication of the Gradle plugin project that the plugin is applied to
 * and uploads the publication to Bintray. Almost all configuration can happen in one central location through the 'plugindev' extension. The
 * PluginDevPlugin ensures that the uploaded publication matches all requirements given by Bintray, JCenter, and the Gradle Plugin Portal.
 */
@SuppressWarnings("UnstableApiUsage")
class PluginDevPlugin implements Plugin<Project> {

    // names
    static final String PLUGINDEV_EXTENSION_NAME = 'plugindev'

    static final String PLUGIN_DEVELOPMENT_GROUP_NAME = 'Plugin development'
    static final String PLUGIN_PUBLISHING_GROUP_NAME = 'Plugin publishing'

    static final String SOURCES_JAR_TASK_NAME = 'sourcesJar'
    static final String DOCS_JAR_TASK_NAME = 'docsJar'
    static final String PLUGIN_DESCRIPTOR_TASK_NAME = 'pluginDescriptorFile'
    static final String PLUGIN_UNDER_TEST_METADATA_TASK_NAME = 'pluginUnderTestMetadata'
    static final String PUBLISH_PLUGIN_TASK_NAME = 'publishPluginToBintray'

    static final String PUBLICATION_NAME = 'plugin'
    static final String JAVA_COMPONENT_NAME = 'java'

    // locations
    static final String MAIN_GENERATED_RESOURCES_LOCATION = 'plugindev/generated-resources/main'
    static final String PLUGIN_DESCRIPTOR_LOCATION = 'META-INF/gradle-plugins'

    // miscellaneous
    private static final String MINIMUM_GRADLE_JAVA_VERSION = '1.8'
    private static final String BINTRAY_USER_DEFAULT_PROPERTY_NAME = 'bintrayUser'
    private static final String BINTRAY_API_KEY_DEFAULT_PROPERTY_NAME = 'bintrayApiKey'

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDevPlugin.class)

    private Project project

    void apply(Project project) {
        // keep the project reference
        this.project = project

        // add a new 'plugindev' extension
        def pluginDevExtension = project.extensions.create(PLUGINDEV_EXTENSION_NAME, PluginDevExtension, this, project)
        LOGGER.debug("Registered extension '$PLUGINDEV_EXTENSION_NAME'")

        // apply the JavaPlugin, MavenPublishPlugin, and BintrayPlugin plugin
        def pluginsToApply = [JavaPlugin, MavenPublishPlugin, BintrayPlugin]
        pluginsToApply.each { Class plugin ->
            project.plugins.apply plugin
            LOGGER.debug("Applied plugin '$plugin.simpleName'")
        }

        // add the JCenter repository
        project.repositories.add(project.repositories.jcenter())
        LOGGER.debug("Added repository 'JCenter'")

        // add the Gradle API dependency to the 'compile' configuration
        project.dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, project.dependencies.gradleApi())
        LOGGER.debug("Added dependency 'Gradle API'")

        // set the source/target compatibility of Java compile and optionally of Groovy compile to 1.6
        JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention.class)
        javaConvention.sourceCompatibility = MINIMUM_GRADLE_JAVA_VERSION
        javaConvention.targetCompatibility = MINIMUM_GRADLE_JAVA_VERSION
        LOGGER.debug("Set source and target compatibility for Java and Groovy to $MINIMUM_GRADLE_JAVA_VERSION")

        // get all the sources from the 'main' source set
        JavaPluginConvention javaPluginConvention = project.convention.findPlugin(JavaPluginConvention)
        def mainSourceSet = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        SourceDirectorySet allMainSources = mainSourceSet.allSource

        // add a task instance that generates a jar with the main sources
        Jar sourcesJarTask = project.tasks.create(SOURCES_JAR_TASK_NAME, Jar.class)
        sourcesJarTask.description = "Assembles a jar archive containing the main source code."
        sourcesJarTask.group = BasePlugin.BUILD_GROUP
        sourcesJarTask.classifier = "sources"
        sourcesJarTask.from(allMainSources)
        LOGGER.debug("Registered task '$sourcesJarTask.name'")

        // add a task instance that generates a jar with the javadoc and optionally with the groovydoc
        Jar docsJarTask = project.tasks.create(DOCS_JAR_TASK_NAME, Jar.class)
        docsJarTask.description = "Assembles a jar archive containing the documentation for the main source code."
        docsJarTask.group = BasePlugin.BUILD_GROUP
        docsJarTask.classifier = "javadoc"
        docsJarTask.into('javadoc') { from project.tasks.findByName(JavaPlugin.JAVADOC_TASK_NAME) }
        project.plugins.withType(GroovyPlugin) {
            docsJarTask.into('groovydoc') { from project.tasks.findByName(GroovyPlugin.GROOVYDOC_TASK_NAME) }
        }
        LOGGER.debug("Registered task '$docsJarTask.name'")

        // add a task instance that generates the plugin descriptor file
        GeneratePluginDescriptorTask pluginDescriptorTask = project.tasks.create(PLUGIN_DESCRIPTOR_TASK_NAME, GeneratePluginDescriptorTask.class)
        pluginDescriptorTask.description = "Generates the plugin descriptor file."
        pluginDescriptorTask.group = PLUGIN_DEVELOPMENT_GROUP_NAME
        pluginDescriptorTask.pluginId = { pluginDevExtension.pluginId }
        pluginDescriptorTask.pluginImplementationClass = { pluginDevExtension.pluginImplementationClass }
        pluginDescriptorTask.pluginVersion = { project.version }
        LOGGER.debug("Registered task '$pluginDescriptorTask.name'")

        // include the plugin descriptor in the main source set
        mainSourceSet.output.dir("$project.buildDir/$MAIN_GENERATED_RESOURCES_LOCATION", builtBy: PLUGIN_DESCRIPTOR_TASK_NAME)

        // ensure the production jar file contains the declared plugin implementation class
        Jar jarTask = project.tasks[JavaPlugin.JAR_TASK_NAME] as Jar
        def findImplementationClass = new ClassFileMatchingAction({ pluginDevExtension.pluginImplementationClass })
        jarTask.filesMatching("**/*.class", findImplementationClass)
        jarTask.doLast({
            if (!findImplementationClass.isFound()) {
                def errorMessage = "Plugin implementation class $pluginDevExtension.pluginImplementationClass must be contained in $jarTask.archivePath."
                throw new IllegalStateException(errorMessage)
            }
        })

        // add a MANIFEST file and optionally a LICENSE file to each jar file (lazily through toString() implementation)
        project.tasks.withType(Jar) { Jar jar ->
            jar.manifest.attributes(
                    'Implementation-Title': new Object() {

                        @Override
                        String toString() {
                            pluginDevExtension.pluginName
                        }
                    },
                    'Implementation-Version': new Object() {

                        @Override
                        String toString() {
                            project.version
                        }
                    },
                    'Implementation-Vendor': new Object() {

                        @Override
                        String toString() {
                            pluginDevExtension.authorName
                        }
                    },
                    'Implementation-Website': new Object() {

                        @Override
                        String toString() {
                            pluginDevExtension.projectUrl
                        }
                    },
                    'Build-Date': new SimpleDateFormat('yyyy-MM-dd').format(new Date()),
                    'Build-JDK': System.getProperty('java.version'),
                    'Build-Gradle': project.gradle.gradleVersion,
                    'Build-PluginDevPlugin': new Object() {

                        @Override
                        String toString() {

                            InputStream resourceAsStream
                            try {
                                resourceAsStream = PluginDevPlugin.getResourceAsStream("/$PLUGIN_DESCRIPTOR_LOCATION/nu.studer.plugindev.properties")
                                def props = new Properties()
                                props.load(resourceAsStream)
                                def version = props.getProperty(GeneratePluginDescriptorTask.IMPLEMENTATION_VERSION_ATTRIBUTE)
                                return version ?: 'unknown'
                            } finally {
                                if (resourceAsStream != null) {
                                    resourceAsStream.close()
                                }
                            }
                        }
                    }
            )
            File license = project.file('LICENSE')
            if (license.exists()) {
                jar.from(license)
            }
            LOGGER.debug("Enhance .jar file of Jar task '$jar.name'")
        }
    }

    def afterExtensionConfiguration(PluginDevExtension extension) {
        // configure the POM configuration closure
        def pomConfig = extension.pomConfiguration ?: {
            name extension.pluginName
            description extension.pluginDescription
            url extension.projectUrl
            inceptionYear extension.projectInceptionYear
            if (extension.pluginLicenses) {
                licenses {
                    extension.pluginLicenses.each { String licenseTypeKey ->
                        def licenseType = Licenses.LICENSE_TYPES[licenseTypeKey]
                        license {
                            name licenseType[0]
                            url licenseType[1]
                            distribution 'repo'
                        }
                    }
                }
            }
            scm {
                url extension.projectVcsUrl
            }
            issueManagement {
                url extension.projectIssuesUrl
            }
            developers {
                developer {
                    id extension.authorId
                    name extension.authorName
                    email extension.authorEmail
                }
            }
        }

        // register a publication that includes the generated artifact, the sources, and the docs
        PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
        def publication = publishing.publications.create(PUBLICATION_NAME, MavenPublication, new Action<MavenPublication>() {

            @Override
            void execute(MavenPublication mavenPublication) {
                mavenPublication.from(project.components.findByName(JAVA_COMPONENT_NAME))
                mavenPublication.artifact(project.tasks[SOURCES_JAR_TASK_NAME], new Action<MavenArtifact>() {

                    @Override
                    void execute(MavenArtifact artifact) {
                        artifact.classifier = "sources"
                    }
                })
                mavenPublication.artifact(project.tasks[DOCS_JAR_TASK_NAME], new Action<MavenArtifact>() {

                    @Override
                    void execute(MavenArtifact artifact) {
                        artifact.classifier = "javadoc"
                    }
                })
                mavenPublication.pom.withXml(new Action<XmlProvider>() {

                    @Override
                    void execute(XmlProvider xmlProvider) {
                        xmlProvider.asNode().children().last() + pomConfig
                    }
                })
            }
        })

        // configure bintray extension
        def bintray = project.extensions.findByType(BintrayExtension)
        bintray.user = project.properties[BINTRAY_USER_DEFAULT_PROPERTY_NAME]
        bintray.key = project.properties[BINTRAY_API_KEY_DEFAULT_PROPERTY_NAME]
        bintray.publications = [publication.name]
        bintray.publish = true
        bintray.dryRun = false
        bintray.pkg {
            name = extension.pluginName
            desc = extension.pluginDescription
            websiteUrl = extension.projectUrl
            issueTrackerUrl = extension.projectIssuesUrl
            vcsUrl = extension.projectVcsUrl
            licenses = extension.pluginLicenses
            labels = extension.pluginTags
            publicDownloadNumbers = true
            version {
                vcsTag = publication.version
                attributes = ['gradle-plugin': "$extension.pluginId:$publication.groupId:$publication.artifactId"]
            }
        }

        // add a task instance that uploads the complete plugin publication to Bintray
        DefaultTask publishPluginTask = project.tasks.create(PUBLISH_PLUGIN_TASK_NAME, DefaultTask.class)
        publishPluginTask.description = "Publishes the complete publication 'plugin' to Bintray."
        publishPluginTask.group = PLUGIN_PUBLISHING_GROUP_NAME
        publishPluginTask.dependsOn project.tasks[BintrayUploadTask.TASK_NAME]
        LOGGER.debug("Registered task '$publishPluginTask.name'")

        // add a task instance that generates the plugin under test metadata file for TesKit
        TaskProvider<PluginUnderTestMetadata> pluginUnderTestMetadataTask = project.tasks.register(PLUGIN_UNDER_TEST_METADATA_TASK_NAME, PluginUnderTestMetadata.class, new Action<PluginUnderTestMetadata>() {

            @Override
            void execute(PluginUnderTestMetadata pluginUnderTestMetadataTask) {
                pluginUnderTestMetadataTask.description = "Generates the plugin metadata file."
                pluginUnderTestMetadataTask.group = PLUGIN_DEVELOPMENT_GROUP_NAME
                pluginUnderTestMetadataTask.outputDirectory.set(project.layout.buildDirectory.dir(pluginUnderTestMetadataTask.name))
                pluginUnderTestMetadataTask.pluginClasspath.from {
                    Configuration gradlePluginConfiguration = project.configurations.detachedConfiguration(project.dependencies.gradleApi())
                    FileCollection gradleApi = gradlePluginConfiguration.incoming.files

                    JavaPluginConvention javaPluginConvention = project.convention.findPlugin(JavaPluginConvention)
                    def mainSourceSet = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                    mainSourceSet.runtimeClasspath.minus(gradleApi)
                }
            }
        })

        // establish TestKit and plugin classpath dependencies
        project.afterEvaluate { Project project ->
            project.normalization.runtimeClasspath.ignore(PluginUnderTestMetadata.METADATA_FILE_NAME)

            project.tasks.withType(Test.class).configureEach(new Action<Test>() {

                @Override
                void execute(Test test) {
                    test.inputs.files(pluginUnderTestMetadataTask.get().pluginClasspath)
                            .withPropertyName("pluginClasspath")
                            .withNormalizer(ClasspathNormalizer.class)
                }
            })

            JavaPluginConvention javaPluginConvention = project.convention.findPlugin(JavaPluginConvention)
            def testSourceSet = javaPluginConvention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
            DependencyHandler dependencies = project.dependencies
            dependencies.add(testSourceSet.compileConfigurationName, dependencies.gradleTestKit())
            dependencies.add(testSourceSet.runtimeConfigurationName, project.layout.files(pluginUnderTestMetadataTask))
        }
    }

}
