gradle-plugindev-plugin
=======================

# Overview

[Gradle](http://www.gradle.org) plugin that facilitates the bundling and uploading 
of Gradle plugins as expected by the [Gradle Plugin Portal](http://plugins.gradle.org/), 
[JCenter](https://bintray.com/bintray/jcenter), and [MavenCentral](http://search.maven.org/).

# Goals

The plugindev plugin takes care of creating the artifacts of a Gradle plugin and of uploading these to Bintray. The 
following high-level goals are driving the functionality of the plugindev plugin: 

 * Compliance of the Gradle plugin artifacts with the Gradle Plugin Portal, JCenter, and MavenCentral must be ensured
 * All boiler-plate configuration to bundle and upload the Gradle plugin artifacts should be avoided
 * All bundle and upload configuration must happen without redundancy
 * Customization of the provided functionality should be possible 
 * High consistency between the representation of different plugins should be achieved
 * Functionality provided by existing plugins and Gradle should be reused as much as possible
 
# Functionality

The following functionality is provided by the plugindev plugin:
 
 * Applies the Java plugin to the project
 * Includes JCenter as a repository for dependency resolution
 * Adds the Gradle API to the 'compile' configuration
 * Sets the compiler source and target compatibility to 1.6, minimum version supported by Gradle
 * Adds a task that puts all main Java and/or Groovy sources into a Jar file
 * Adds a task that puts all Javadoc and/or Groovydoc into a Jar file
 * Adds a task that generates the plugin descriptor file
 * Includes the generated plugin descriptor file in the production Jar file
 * Validates the plugin implementation class declared in the plugin descriptor file is contained in the production Jar file
 * Adds a manifest file to each Jar file with meta data about the plugin
 * Adds a license file to each Jar file if available in the root of the project
 * Creates a POM file with the required metadata derived from the plugin configuration
 * Creates a publication with the production Jar file, sources Jar file, documentation Jar file, and the POM file
 * Publishes the bundled plugin artifacts to Bintray as a new version to a new or existing package
 * Ensures the published version has the required Bintray attributes set

# Design

The plugindev plugin creates all the required artifacts through Gradle core tasks. The configuration of these 
artifacts happens in a central place through the `plugindev` extension. The 
[MavenPublishPlugin](http://www.gradle.org/docs/current/userguide/publishing_maven.html) is leveraged to create 
a publication of these artifacts.

The configuration of the metadata at the publication target (Bintray) happens through the `plugindev` extension and 
the `bintray` extension. The [BintrayPlugin](https://github.com/bintray/gradle-bintray-plugin) is leveraged to publish 
the artifacts to Bintray.
 
# Configuration

## Apply plugindev plugin

Apply the `nu.studer.plugindev` plugin to your Gradle plugin project. Make sure to also 
apply the `groovy` plugin if you intend to write your plugin in Groovy. 

### Gradle 1.x and 2.0

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'nu.studer:gradle-plugindev-plugin:1.0.0'
    }
}

apply plugin: 'nu.studer.plugindev'
```

### Gradle 2.1 and higher

```groovy
plugins {
  id 'nu.studer.plugindev' version '1.0.0'
}
```

Please refer to the [Gradle DSL PluginDependenciesSpec](http://www.gradle.org/docs/current/dsl/org.gradle.plugin.use.PluginDependenciesSpec.html) to 
understand the behavior and limitations when using the new syntax to declare plugin dependencies.

## Set group and version

Set the `group` and `version` of your Gradle plugin project. By default, Gradle derives 
the name of the project from the containing folder. A custom project name could be set during 
the initialization phase in the settings.gradle file.

```groovy
// make sure to define the group and version before the 'plugindev' configuration
group = 'org.example'
version = '0.0.1.DEV'
```

## Configure plugindev plugin

Configure the plugindev plugin through the `plugindev` extension.

### When building and uploading a new plugin

Provide the minimum set of configuration properties and let the plugindev plugin derive
the values for the remaining attributes. This will also ensure the highest degree of
consistency. The complete set of configuration properties is shown and explained further down.  

```groovy
plugindev {
    pluginImplementationClass 'org.example.gradle.foo.FooPlugin'
    pluginDescription 'Gradle plugin that does foo.'
    pluginLicenses 'Apache-2.0'
    pluginTags 'gradle', 'plugin', 'foo'
    authorId 'homer'
    authorName 'Homer Simpson'
    authorEmail 'homer@simpson.org'
    projectUrl 'https://github.com/homer/gradle-foo-plugin'
    projectInceptionYear '2014'
    done()
}
```

In the example above, it is assumed that the plugin is hosted on [GitHub](https://github.com/). Thus, 
the configuration properties for the issue url and the vcs url are automatically derived by the 
plugindev plugin.

### When building and uploading an existing plugin

Provide the default set of configuration properties that match the setup of your current Gradle 
plugin project and let the plugindev plugin derive the values for the remaining attributes. The 
complete set of configuration properties is shown and explained further down.  

```groovy
plugindev {
    pluginId = 'org.example.foo'
    pluginImplementationClass 'org.example.gradle.foo.FooPlugin'
    pluginDescription 'Gradle plugin that does foo.'
    pluginLicenses 'Apache-2.0'
    pluginTags 'gradle', 'plugin', 'foo'
    authorId 'homer'
    authorName 'Homer Simpson'
    authorEmail 'homer@simpson.org'
    projectUrl 'https://simpson.org/homer/gradle-foo-plugin'
    projectIssuesUrl 'https://simpson.org/gradle-foo-plugin/issue-tracking'
    projectVcsUrl 'https://simpson.org/gradle-foo-plugin/svn'
    projectInceptionYear '2014'
    done()
}
```

In the example above, no assumptions are made about where your project is hosted. Thus, the configuration properties 
for the issue url and the vcs url must be declared explicitly.

## Configure bintray plugin

Provide the remaining bintray configuration through the `bintray` extension. A 
good place to store the bintray credentials is the gradle.properties file in your Gradle 
user home directory. 

```groovy
bintray {
    user = "$BINTRAY_USER"
    key = "$BINTRAY_API_KEY"
    pkg.repo = 'gradle-plugins'
}
```

## Run publishPluginToBintray task

Run the `publishPluginToBintray` Gradle task which will build the plugin artifacts and publish them 
to Bintray. Use the `-i` option to get more detailed feedback about the bundling and publishing progress. 

```console
gradle publishPluginToBintray
```

## Complete example

The previously explained configuration steps lead to the following functional build file. 

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "nu.studer:gradle-plugindev-plugin:0.0.12"
    }
}

apply plugin: 'nu.studer.plugindev'

group = 'org.example'
version = '0.0.1.DEV'

plugindev {
    pluginImplementationClass 'org.example.gradle.foo.FooPlugin'
    pluginDescription 'Gradle plugin that does foo.'
    pluginLicenses 'Apache-2.0'
    pluginTags 'gradle', 'plugin', 'foo'
    authorId 'homer'
    authorName 'Homer Simpson'
    authorEmail 'homer@simpson.org'
    projectUrl 'https://github.com/homer/gradle-foo-plugin'
    projectInceptionYear '2014'
    done()
}

bintray {
    user = "$BINTRAY_USER"
    key = "$BINTRAY_API_KEY"
    pkg.repo = 'gradle-plugins'
}
```

You can also find the complete examples [for 1.x and 2.0](example/minimal_config_pre_2-1/build.gradle) and 
[for 2.1 and newer](example/minimal_config_from_2-1/build.gradle) on GitHub.

# Customization

TBD

You can also find the complete examples [with plugindev configuration](example/plugindev_config/build.gradle) and 
[with bintray configuration ](example/bintray_config/build.gradle) on GitHub.

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License
This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
