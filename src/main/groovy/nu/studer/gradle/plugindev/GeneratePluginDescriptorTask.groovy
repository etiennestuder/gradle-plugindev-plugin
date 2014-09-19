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
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to generate the Gradle plugin descriptor file.
 */
class GeneratePluginDescriptorTask extends DefaultTask {

    static final def IMPLEMENTATION_CLASS_ATTRIBUTE = 'implementation-class'
    static final def IMPLEMENTATION_VERSION_ATTRIBUTE = 'implementation-version'

    @Input
    def pluginId

    @Input
    def pluginImplementationClass

    @Input
    def pluginVersion

    @OutputFile
    public File getPropertiesFile() {
        def resolvedPluginId = Closures.resolveAsString(pluginId)
        project.file("${project.buildDir}/plugindev/${resolvedPluginId}.properties")
    }

    @TaskAction
    def generate() {
        def resolvedPluginImplementationClass = Closures.resolveAsString(pluginImplementationClass)
        def resolvedPluginVersion = Closures.resolveAsString(pluginVersion)

        def properties = new LinkedHashMap()
        properties.put(IMPLEMENTATION_CLASS_ATTRIBUTE, resolvedPluginImplementationClass)
        properties.put(IMPLEMENTATION_VERSION_ATTRIBUTE, resolvedPluginVersion)

        BufferedWriter bw = null
        try {
            bw = new BufferedWriter(new FileWriter(propertiesFile))
            properties.each {
                bw.write "$it.key=$it.value" as String
                bw.newLine()
            }
            bw.flush()
        } finally {
            if (bw != null) {
                bw.close()
            }
        }
    }

}
