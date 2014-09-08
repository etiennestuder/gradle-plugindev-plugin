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
import nu.studer.gradle.util.Licenses
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Plugin that extends the Java plugin, adds a task to create a sources jar, and adds a task to create a documentation jar.
 */
class PluginDevPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDevPlugin.class);

    private static final String MINIMUM_GRADLE_JAVA_VERSION = '1.6'
    private static final String SOURCES_JAR_TASK_NAME = 'sourcesJar'
    private static final String DOCS_JAR_TASK_NAME = 'docsJar'
    private static final String GENERATE_PLUGIN_DESCRIPTOR_FILE_TASK_NAME = 'generatePluginDescriptorFile'
    private static final String PUBLICATION_NAME = 'mavenJava'
    private static final String JAVA_COMPONENT_NAME = 'java'

    private Project project

    public void apply(Project project) {
        // keep the project reference
        this.project = project

        // add a new 'plugindev' extension
        def pluginDevExtension = project.extensions.create(PluginDevConstants.PLUGINDEV_EXTENSION_NAME, PluginDevExtension, this, project)
        LOGGER.debug("Registered extension '$PluginDevConstants.PLUGINDEV_EXTENSION_NAME'")

        // apply the Java plugin
        project.plugins.apply(JavaPlugin)
        LOGGER.debug("Applied plugin 'JavaPlugin'")

        // apply the MavenPublishPlugin plugin
        project.plugins.apply(MavenPublishPlugin)
        LOGGER.debug("Applied plugin 'MavenPublishPlugin'")

        // apply the BintrayPlugin plugin
        project.plugins.apply(BintrayPlugin)
        LOGGER.debug("Applied plugin 'BintrayPlugin'")

        // add the JCenter repository
        project.repositories.add(project.repositories.jcenter())
        LOGGER.debug("Added repository 'JCenter'")

        // add the Gradle API dependency to the 'compile' configuration
        project.dependencies.add(JavaPlugin.COMPILE_CONFIGURATION_NAME, project.dependencies.gradleApi())
        LOGGER.debug("Added dependency 'Gradle API'")

        // set the source/target compatibility of Java compile and optionally of Groovy compile to 1.6
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
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
        String docsJarTaskName = DOCS_JAR_TASK_NAME
        Jar docsJarTask = project.tasks.create(docsJarTaskName, Jar.class)
        docsJarTask.description = "Assembles a jar archive containing the documentation for the main source code."
        docsJarTask.group = BasePlugin.BUILD_GROUP
        docsJarTask.classifier = "javadoc"
        docsJarTask.into('javadoc') { from project.tasks.findByName(JavaPlugin.JAVADOC_TASK_NAME) }
        project.plugins.withType(GroovyPlugin) {
            docsJarTask.into('groovydoc') { from project.tasks.findByName(GroovyPlugin.GROOVYDOC_TASK_NAME) }
        }
        LOGGER.debug("Registered task '$docsJarTask.name'")

        // add a task instance that generates the plugin descriptor file
        String generatePluginDescriptorFileTaskName = GENERATE_PLUGIN_DESCRIPTOR_FILE_TASK_NAME
        GeneratePluginDescriptorTask generatePluginDescriptorFile = project.tasks.create(generatePluginDescriptorFileTaskName, GeneratePluginDescriptorTask.class)
        generatePluginDescriptorFile.description = "Generates the plugin descriptor file."
        generatePluginDescriptorFile.group = BasePlugin.BUILD_GROUP
        generatePluginDescriptorFile.pluginId = { pluginDevExtension.pluginId }
        generatePluginDescriptorFile.pluginImplementationClass = { pluginDevExtension.pluginImplementationClass }
        LOGGER.debug("Registered task '$generatePluginDescriptorFile.name'")

        // include the plugin descriptor in the production jar file
        project.tasks[JavaPlugin.JAR_TASK_NAME].into('META-INF/gradle-plugins') { from generatePluginDescriptorFile }

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
                    'Build-Date': new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                    'Build-JDK': System.getProperty('java.version'),
                    'Build-Gradle': project.gradle.gradleVersion
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
            scm {
                url extension.projectVcsUrl
            }
            if (extension.pluginLicenses) {
                licenses {
                    extension.pluginLicenses.each { String licenseTypeKey ->
                        def licenseType = Licenses.LICENSE_TYPES[licenseTypeKey]
                        license {
                            name licenseType[0]
                            url licenseType[1]
                        }
                    }
                }
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
        bintray.publications = [publication.getName()]
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
    }

}
