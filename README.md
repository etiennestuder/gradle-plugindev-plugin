gradle-plugindev-plugin
=======================

# Overview

[Gradle](http://www.gradle.org) plugin that facilitates the bundling and uploading 
of Gradle plugins as expected by the Gradle Plugin Portal, JCenter, and MavenCentral.

# Goals

The plugindev plugin takes care of creating the artifacts of a Gradle plugin and of uploading these to Bintray. The 
following high-level goals are driving the functionality of the plugindev plugin: 

 * Compliance of the Gradle plugin artifacts with the Gradle Plugin Portal, JCenter, and MavenCentral must be ensured
 * All boiler-plate configuration to bundle and upload the Gradle plugin artifacts should be avoided
 * All bundle and upload configuration must happen without redundancy
 * Customization of the provided functionality should be possible 
 * High consistency between the representation of different plugins should be achieved
 
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

Configure the plugindev plugin through the `plugindev` configuration block.

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
    projectUrl 'https://github.com/homer/gradle-foo-plugin'
    projectInceptionYear = '2014'
    done()
}
```
## Configure bintray plugin

Provide the remaining bintray configuration through the `bintray` configuration block. A 
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

Run the `publishPluginToBintray` Gradle task which will build and upload the Gradle plugin artifacts. Use 
the `-i` option to get more detailed feedback about the bundling and uploading process. 

```console
gradle publishPluginToBintray
```

You can find the complete example [here for 1.x and 2.0](example/minimal_config_pre_2-1/build.gradle) and 
[here for 2.1 and newer](example/minimal_config_from_2-1/build.gradle).

## Complete example

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
    authorId 'etiennestuder'
    authorName 'Etienne Studer'
    authorEmail 'etienne@example.org'
    projectUrl 'https://github.com/etiennestuder/gradle-foo-plugin'
    projectInceptionYear '2014'
    done()
}

bintray {
    user = "$BINTRAY_USER"
    key = "$BINTRAY_API_KEY"
    pkg.repo = 'gradle-plugins'
}
```

# Customization

TBD

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License
This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
