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
/**
 * Extension point to configure the plugin development plugin.
 */
class PluginDevExtension {

    private final PluginDevPlugin plugin

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

    PluginDevExtension(PluginDevPlugin plugin) {
        this.plugin = plugin
        this.pluginLicenses = new TreeSet<>()
        this.pluginTags = new TreeSet<>()
    }

    boolean done() {
        // use defaults in case of github project
        if (projectUrl?.startsWith('https://github.com')) {
            if (!projectIssuesUrl) {
                projectIssuesUrl = "${projectUrl}/issues"
            }
            if (!projectVcsUrl) {
                projectVcsUrl = "${projectUrl}.git"
            }
        }

        // todo check for non-null values
        // todo check implementation class exists in jar
        this.plugin.afterExtensionConfiguration(this)
    }

    def setPluginLicense(String license) {
        this.pluginLicenses.clear()
        this.pluginLicenses.add license
    }

    def setPluginLicenses(List<String> licenses) {
        this.pluginLicenses.clear()
        this.pluginLicenses.addAll licenses
    }

    def pluginLicense(String license) {
        this.pluginLicenses.add license
    }

    def pluginLicenses(String... licenses) {
        this.pluginLicenses.addAll licenses
    }

    def setPluginTag(String tag) {
        this.pluginTags.clear()
        this.pluginTags.add tag
    }

    def setPluginTags(List<String> tags) {
        this.pluginTags.clear()
        this.pluginTags.addAll tags
    }

    def pluginTag(String tag) {
        this.pluginTags.add tag
    }

    def pluginTags(String... tags) {
        this.pluginTags.addAll tags
    }

}
