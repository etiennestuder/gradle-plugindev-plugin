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

import org.gradle.api.Project

/**
 * Extension point to configure the plugin development plugin.
 */
class PluginDevExtension {

    private final PluginDevPlugin plugin
    private final Project project

    String pluginId
    String pluginName
    String pluginDescription
    String pluginImplementationClass
    SortedSet<String> pluginLicenses
    SortedSet<String> pluginTags
    String authorId
    String authorName
    String authorEmail
    String projectUrl
    String projectIssuesUrl
    String projectVcsUrl
    String projectInceptionYear
    Closure pomConfiguration

    PluginDevExtension(PluginDevPlugin plugin, Project project) {
        this.plugin = plugin
        this.project = project
        this.pluginLicenses = new TreeSet<>()
        this.pluginTags = new TreeSet<>()
    }

    def setPluginLicense(String license) {
        pluginLicenses.clear()
        pluginLicenses.add license
    }

    def setPluginLicenses(List<String> licenses) {
        pluginLicenses.clear()
        pluginLicenses.addAll licenses
    }

    def pluginLicense(String license) {
        pluginLicenses.add license
    }

    def pluginLicenses(String... licenses) {
        pluginLicenses.addAll licenses
    }

    def setPluginTag(String tag) {
        pluginTags.clear()
        pluginTags.add tag
    }

    def setPluginTags(List<String> tags) {
        pluginTags.clear()
        pluginTags.addAll tags
    }

    def pluginTag(String tag) {
        pluginTags.add tag
    }

    def pluginTags(String... tags) {
        pluginTags.addAll tags
    }

    def done() {
        // use default in case of missing plugin id
        if (!pluginId) {
            if (pluginImplementationClass?.indexOf('.') > -1) {
                pluginId = pluginImplementationClass.
                        substring(0, pluginImplementationClass.lastIndexOf('.')).
                        replaceAll('.gradle', '')
            }
        }

        // use default in case of missing plugin name
        if (!pluginName) {
            pluginName = project.name
        }

        // use defaults for github project in case of missing issues url
        if (!projectIssuesUrl) {
            if (projectUrl?.startsWith('https://github.com')) {
                projectIssuesUrl = "${projectUrl}/issues"
            }
        }

        // use defaults for github project in case of missing vcs url
        if (!projectVcsUrl) {
            if (projectUrl?.startsWith('https://github.com')) {
                projectVcsUrl = "${projectUrl}.git"
            }
        }

        // todo check for non-null values
        // todo check implementation class exists in jar
        plugin.afterExtensionConfiguration this
    }

}
