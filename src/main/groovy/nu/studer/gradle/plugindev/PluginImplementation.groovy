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

import groovy.transform.EqualsAndHashCode

import static nu.studer.gradle.plugindev.PluginChecks.checkPropertyNotEmpty

@EqualsAndHashCode
class PluginImplementation implements Serializable {
    private static final long serialVersionUID = 1L;

    String pluginId
    String pluginImplementationClass

    void pluginId(String pluginId) {
        this.pluginId = pluginId
    }

    void pluginImplementationClass(String pluginImplementationClass) {
        this.pluginImplementationClass = pluginImplementationClass
    }

    public void applyDefaultValuesForEmptyValues() {
        // use default in case of missing plugin id, derived from package path of plugin class minus 'gradle' & 'plugin' packages
        if (!pluginId) {
            if (pluginImplementationClass?.indexOf('.') > -1) {
                pluginId = pluginImplementationClass.
                        substring(0, pluginImplementationClass.lastIndexOf('.')).
                        replaceAll('\\.gradle\\.', '.').
                        replaceAll('\\.gradle$', '').
                        replaceAll('\\.plugin\\.', '.').
                        replaceAll('\\.plugin$', '')
            }
        }
    }

    public void checkPropertiesHaveNonEmptyValues() {
        checkPropertyNotEmpty(pluginId, 'pluginId')
        checkPropertyNotEmpty(pluginImplementationClass, 'pluginImplementationClass')
    }

    public void checkPropertiesHaveValidValues() {
        // ensure fully qualified plugin id
        if (!pluginId.contains('.')) {
            throw new IllegalStateException("Property 'pluginId' must be a fully qualified id: $pluginId")
        }
    }

    @Override
    def String toString() {
        return "pluginId='$pluginId'" +
                ", pluginImplementationClass='$pluginImplementationClass'"
    }
}
