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

import nu.studer.gradle.util.Closures
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

import static nu.studer.gradle.plugindev.PluginDevPlugin.MAIN_GENERATED_RESOURCES_LOCATION
import static nu.studer.gradle.plugindev.PluginDevPlugin.PLUGIN_DESCRIPTOR_LOCATION
import static nu.studer.java.util.OrderedProperties.OrderedPropertiesBuilder

/**
 * Task to generate the Gradle plugin descriptor files.
 */
class GeneratePluginDescriptorsTask extends DefaultTask {

    static final def IMPLEMENTATION_CLASS_ATTRIBUTE = 'implementation-class'
    static final def IMPLEMENTATION_VERSION_ATTRIBUTE = 'implementation-version'

    @Input
    def pluginImplementations

    @Input
    def pluginVersion

    @OutputFiles
    public Collection<File> getPropertiesFiles() {
        getPluginImplementations().collect {
            getPropertiesFile(it)
        }
    }

    public File getPropertiesFile(def pluginImplementation) {
        project.file("$project.buildDir/$MAIN_GENERATED_RESOURCES_LOCATION/$PLUGIN_DESCRIPTOR_LOCATION/${pluginImplementation.pluginId}.properties")
    }

    def writeDescriptor(def pluginImplementation, def resolvedPluginVersion) {
        def properties = new OrderedPropertiesBuilder().withSuppressDateInComment(true).build()
        properties.setProperty(IMPLEMENTATION_CLASS_ATTRIBUTE, pluginImplementation.pluginImplementationClass)
        properties.setProperty(IMPLEMENTATION_VERSION_ATTRIBUTE, resolvedPluginVersion)

        Writer writer = null
        try {
            writer = new FileWriter(getPropertiesFile(pluginImplementation))
            properties.store(writer, null)
        } finally {
            if (writer != null) {
                writer.close()
            }
        }
    }

    @TaskAction
    def generate() {
        def resolvedPluginVersion = Closures.resolveAsString(pluginVersion)
        getPluginImplementations().each {
            writeDescriptor(it, resolvedPluginVersion)
        }
    }
}
