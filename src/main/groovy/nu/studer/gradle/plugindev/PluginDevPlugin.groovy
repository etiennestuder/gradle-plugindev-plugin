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
import org.gradle.api.plugins.*
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Plugin that extends the Java plugin, adds a task to create a sources jar, and adds a task to create a documentation jar.
 */
class PluginDevPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDevPlugin.class);

    public void apply(Project project) {
        // apply the Java plugin
        project.plugins.apply(JavaPlugin.class)
        LOGGER.debug("Applied plugin 'JavaPlugin'")

        // add the JCenter repository
        project.repositories.add(project.repositories.jcenter())
        LOGGER.debug("Added repository 'JCenter'")

        // add the Gradle API dependency
        project.dependencies.add(JavaPlugin.COMPILE_CONFIGURATION_NAME, project.dependencies.gradleApi())
        LOGGER.debug("Added dependency 'Gradle API'")

        // add a new 'plugindev' extension
        project.extensions.create(PluginDevConstants.PLUGINDEV_EXTENSION_NAME, PluginDevExtension, project)
        LOGGER.debug("Registered extension '$PluginDevConstants.PLUGINDEV_EXTENSION_NAME'")

        // get all the sources from the 'main' source set
        JavaPluginConvention javaPluginConvention = project.convention.findPlugin(JavaPluginConvention)
        def mainSourceSet = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        SourceDirectorySet allMainSources = mainSourceSet.allSource

        // add a task instance that generates a jar with the main sources
        String sourcesJarTaskName = "sourcesJar"
        Jar sourcesJarTask = project.tasks.create(sourcesJarTaskName, Jar.class)
        sourcesJarTask.description = "Assembles a jar archive containing the main sources."
        sourcesJarTask.group = BasePlugin.BUILD_GROUP
        sourcesJarTask.from(allMainSources)
        LOGGER.debug("Registered task '$sourcesJarTask.name'")

        // add a task instance that generates a jar with the main javadoc
        String docsJarTaskName = "docsJar"
        Jar docsJarTask = project.tasks.create(docsJarTaskName, Jar.class)
        docsJarTask.description = "Assembles a jar archive containing the source documentation."
        docsJarTask.group = JavaBasePlugin.DOCUMENTATION_GROUP
        docsJarTask.into('javadoc') { from project.tasks.findByName(JavaPlugin.JAVADOC_TASK_NAME) }
        project.plugins.withType(GroovyPlugin) {
            docsJarTask.into('groovydoc') { from project.tasks.findByName(GroovyPlugin.GROOVYDOC_TASK_NAME) }
        }
        LOGGER.debug("Registered task '$docsJarTask.name'")
    }

}
