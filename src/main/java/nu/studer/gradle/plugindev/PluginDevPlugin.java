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

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
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

    // task names
    public static final String SOURCES_JAR_TASK_NAME = "sourcesJar";
    public static final String DOCS_JAR_TASK_NAME = "docsJar";

    // miscellaneous
    private static final JavaVersion MINIMUM_GRADLE_JAVA_VERSION = JavaVersion.VERSION_1_8;

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDevPlugin.class);

    public void apply(Project project) {
        // apply the JavaGradlePluginPlugin
        Class<JavaGradlePluginPlugin> pluginClass = JavaGradlePluginPlugin.class;
        project.getPlugins().apply(pluginClass);
        LOGGER.debug("Applied plugin '" + pluginClass.getSimpleName() + "'");

        // add the MavenCentral repository
        RepositoryHandler repositories = project.getRepositories();
        repositories.add(repositories.mavenCentral());
        LOGGER.debug("Added repository 'MavenCentral'");

        // set the source/target compatibility of Java compile and optionally of Groovy compile
        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        javaExtension.setSourceCompatibility(MINIMUM_GRADLE_JAVA_VERSION);
        javaExtension.setTargetCompatibility(MINIMUM_GRADLE_JAVA_VERSION);
        LOGGER.debug("Set Java source and target compatibility to " + MINIMUM_GRADLE_JAVA_VERSION);

        // get all the sources from the 'main' source set
        SourceSet mainSourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceDirectorySet allMainSources = mainSourceSet.getAllSource();

        // add a task instance that generates a jar with the main sources
        project.getTasks().register(SOURCES_JAR_TASK_NAME, Jar.class, jar -> {
            jar.setDescription("Assembles a jar archive containing the main source code.");
            jar.setGroup(BasePlugin.BUILD_GROUP);
            jar.getArchiveClassifier().set("sources");
            jar.from(allMainSources);
        });
        LOGGER.debug("Registered task '" + SOURCES_JAR_TASK_NAME + "'");

        // add a task instance that generates a jar with the javadoc
        project.getTasks().register(DOCS_JAR_TASK_NAME, Jar.class, jar -> {
            jar.setDescription("Assembles a jar archive containing the documentation for the main source code.");
            jar.setGroup(BasePlugin.BUILD_GROUP);
            jar.getArchiveClassifier().set("javadoc");
            jar.into("javadoc").from(project.getTasks().findByName(JavaPlugin.JAVADOC_TASK_NAME));
        });
        LOGGER.debug("Registered task '" + DOCS_JAR_TASK_NAME + "'");

        // add a MANIFEST file with basic build implementation
        HashMap<String, String> attrs = new HashMap<>();
        attrs.put("Build-Date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        attrs.put("Build-JDK", System.getProperty("java.version"));
        attrs.put("Build-Gradle", project.getGradle().getGradleVersion());
        project.getTasks().withType(Jar.class).configureEach(jar -> jar.getManifest().attributes(attrs));
        LOGGER.debug("Configured jar files with manifest attributes");
    }

}
