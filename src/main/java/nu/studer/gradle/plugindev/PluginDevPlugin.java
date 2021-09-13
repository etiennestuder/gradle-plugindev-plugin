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
package nu.studer.gradle.plugindev;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
import org.gradle.plugin.devel.tasks.ValidatePlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Plugin for Gradle plugin development. The PluginDevPlugin creates a MavenPublication of the Gradle plugin project that the plugin is applied to. Almost all configuration can
 * happen in one central location through the 'plugindev' extension. The PluginDevPlugin ensures that the publication matches all requirements given by MavenCentral and the Gradle
 * Plugin Portal.
 */
@SuppressWarnings("unused")
public class PluginDevPlugin implements Plugin<Project> {

    // miscellaneous
    private static final JavaLanguageVersion TOOLCHAIN_LANGUAGE_VERSION = JavaLanguageVersion.of(8);

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDevPlugin.class);

    public void apply(Project project) {
        // add the MavenCentral repository
        RepositoryHandler repositories = project.getRepositories();
        repositories.add(repositories.mavenCentral());
        LOGGER.debug("Added repository 'MavenCentral'");

        // apply the JavaGradlePluginPlugin
        Class<JavaGradlePluginPlugin> pluginClass = JavaGradlePluginPlugin.class;
        project.getPlugins().apply(pluginClass);
        LOGGER.debug("Applied plugin '" + pluginClass.getSimpleName() + "'");

        // set default toolchain for compilation and running tests
        project.getExtensions().getByType(JavaPluginExtension.class).getToolchain().getLanguageVersion().set(TOOLCHAIN_LANGUAGE_VERSION);

        // add a MANIFEST file with basic build implementation
        HashMap<String, String> attrs = new HashMap<>();
        attrs.put("Build-Date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        attrs.put("Build-JDK", System.getProperty("java.version"));
        attrs.put("Build-Gradle", project.getGradle().getGradleVersion());
        project.getTasks().withType(Jar.class).configureEach(jar -> jar.getManifest().attributes(attrs));
        LOGGER.debug("Configured jar files with manifest attributes");

        // enable strict validation
        project.getTasks().withType(ValidatePlugins.class).configureEach(t -> {
                t.getFailOnWarning().set(true);
                t.getEnableStricterValidation().set(true);
            }
        );
    }

}
