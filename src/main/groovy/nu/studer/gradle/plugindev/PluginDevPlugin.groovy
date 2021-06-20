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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.ClasspathNormalizer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.gradle.util.GradleVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Plugin for Gradle plugin development. The PluginDevPlugin creates a MavenPublication of the Gradle plugin project that the plugin is applied to.
 * Almost all configuration can happen in one central location through the 'plugindev' extension. The PluginDevPlugin ensures that the publication
 * matches all requirements given by MavenCentral and the Gradle Plugin Portal.
 */
class PluginDevPlugin implements Plugin<Project> {

    // names
    static final String PLUGIN_DEVELOPMENT_GROUP_NAME = 'Plugin development'

    static final String SOURCES_JAR_TASK_NAME = 'sourcesJar'
    static final String DOCS_JAR_TASK_NAME = 'docsJar'
    static final String PLUGIN_DESCRIPTOR_TASK_NAME = 'pluginDescriptorFile'
    static final String PLUGIN_UNDER_TEST_METADATA_TASK_NAME = 'pluginUnderTestMetadata'

    // locations
    static final String MAIN_GENERATED_RESOURCES_LOCATION = 'plugindev/generated-resources/main'
    static final String PLUGIN_DESCRIPTOR_LOCATION = 'META-INF/gradle-plugins'

    // miscellaneous
    private static final String MINIMUM_GRADLE_JAVA_VERSION = '1.8'

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDevPlugin.class)

    private Project project

    void apply(Project project) {
        // keep the project reference
        this.project = project

        // keep a local variable given the many usages in this method
        DependencyHandler dependencies = project.dependencies

        // apply the JavaGradlePluginPlugin
        def pluginsToApply = [JavaGradlePluginPlugin]
        pluginsToApply.each { Class plugin ->
            project.plugins.apply plugin
            LOGGER.debug("Applied plugin '$plugin.simpleName'")
        }

        // add the MavenCentral repository
        project.repositories.add(project.repositories.mavenCentral())
        LOGGER.debug("Added repository 'MavenCentral'")

        // set the source/target compatibility of Java compile and optionally of Groovy compile
        JavaPluginConvention javaPluginConvention = project.convention.getPlugin(JavaPluginConvention.class)
        javaPluginConvention.sourceCompatibility = MINIMUM_GRADLE_JAVA_VERSION
        javaPluginConvention.targetCompatibility = MINIMUM_GRADLE_JAVA_VERSION
        LOGGER.debug("Set source and target compatibility for Java and Groovy to $MINIMUM_GRADLE_JAVA_VERSION")

        // get all the sources from the 'main' source set
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

        // add a task instance that generates the plugin under test metadata file for TestKit
        BackwardCompatiblePluginUnderTestMetadata pluginUnderTestMetadataTask = project.tasks.create(PLUGIN_UNDER_TEST_METADATA_TASK_NAME, BackwardCompatiblePluginUnderTestMetadata.class)
        pluginUnderTestMetadataTask.description = "Generates the plugin metadata file."
        pluginUnderTestMetadataTask.group = PLUGIN_DEVELOPMENT_GROUP_NAME
        pluginUnderTestMetadataTask.outputDirectory = project.file("$project.buildDir/$pluginUnderTestMetadataTask.name")
        Configuration gradlePluginConfiguration = project.configurations.detachedConfiguration(dependencies.gradleApi())
        FileCollection gradleApi = gradlePluginConfiguration.incoming.files
        pluginUnderTestMetadataTask.pluginClasspath = mainSourceSet.runtimeClasspath.minus(gradleApi)

        // establish TestKit and plugin classpath dependencies in a Gradle version specific way
        project.afterEvaluate { Project proj ->
            if (GradleVersion.current() >= GradleVersion.version('4.0')) {
                proj.normalization.runtimeClasspath.ignore(PluginUnderTestMetadata.METADATA_FILE_NAME)
            }

            proj.tasks.withType(Test.class).all(new Action<Test>() {

                @Override
                void execute(Test test) {
                    if (GradleVersion.current() >= GradleVersion.version('4.3')) {
                        test.inputs.files(pluginUnderTestMetadataTask.pluginClasspath)
                            .withPropertyName("pluginClasspath")
                            .withNormalizer(ClasspathNormalizer.class)
                    } else {
                        test.inputs.files(pluginUnderTestMetadataTask.pluginClasspath)
                            .withPropertyName("pluginClasspath")
                    }
                }
            })

            def testSourceSet = javaPluginConvention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
            dependencies.add(testSourceSet.implementationConfigurationName, dependencies.gradleTestKit())
            dependencies.add(testSourceSet.runtimeOnlyConfigurationName, pluginUnderTestMetadataTask.outputs.files)
        }
    }

}
