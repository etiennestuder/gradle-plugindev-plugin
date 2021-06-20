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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Plugin for Gradle plugin development. The PluginDevPlugin creates a MavenPublication of the Gradle plugin project that the plugin is applied to.
 * Almost all configuration can happen in one central location through the 'plugindev' extension. The PluginDevPlugin ensures that the publication
 * matches all requirements given by MavenCentral and the Gradle Plugin Portal.
 */
class PluginDevPlugin implements Plugin<Project> {

    // task names
    static final String SOURCES_JAR_TASK_NAME = 'sourcesJar'
    static final String DOCS_JAR_TASK_NAME = 'docsJar'

    // miscellaneous
    private static final String MINIMUM_GRADLE_JAVA_VERSION = '1.8'

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDevPlugin.class)

    void apply(Project project) {
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
            )
            File license = project.file('LICENSE')
            if (license.exists()) {
                jar.from(license)
            }
            LOGGER.debug("Enhance .jar file of Jar task '$jar.name'")
        }
    }

}
