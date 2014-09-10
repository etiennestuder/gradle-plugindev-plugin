gradle-plugindev-plugin
=======================

# Overview

[Gradle](http://www.gradle.org) plugin that facilitates the bundling and uploading 
of Gradle plugins as expected by the Gradle Plugin Portal, JCenter, and MavenCentral.

# Goals

The gradle-plugindev plugin takes care of creating the artifacts of a Gradle plugin and of uploading these to Bintray. The 
following high-level goals are driving the functionality of the gradle-plugindev plugin: 

 * Compliance of the Gradle plugin artifacts with the Gradle Plugin Portal, JCenter, and MavenCentral must be ensured
 * All boiler-plate configuration to bundle and upload the Gradle plugin artifacts should be avoided
 * All bundle and upload configuration must happen without redundancy
 * Customization of the provided functionality should be possible 
 * High consistency between the representation of different plugins should be achieved
 
# Functionality

The following functionality is provided by the gradle-plugindev plugin:
 
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
 * Publishes the bundled plugin artifacts to Bintray as a new version and optionally in a new package
 * Ensures the published version has the required Bintray attributes set

# Configuration

## Apply gradle-plugindev plugin

Apply the gradle-plugindev plugin to your Gradle plugin project. Make sure to also 
apply the 'groovy' plugin if you intend to write your plugin in Groovy. 

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "nu.studer:gradle-plugindev-plugin:1.0.0"
    }
}
apply plugin: 'nu.studer.plugindev'
```

## Set group and version

Set the group and version of your Gradle plugin project. By default, the name of 
the project is derived from the containing folder by Gradle. A custom name 
could be set during the initialization phase in the settings.gradle file.

```groovy
group = 'org.example'
version = '0.0.1.DEV'
```

## Configure gradle-plugindev plugin

Configure the gradle-plugindev plugin through the `plugindev` configuration block.

### When building and uploading a new plugin

Provide the minimum set of configuration properties and let the plugindev-plugin derive
the values for the remaining attributes. This will also ensure the highest degree of
consistency. The complete set of configuration properties is shown and explained further down.  

```groovy
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
```

### When building and uploading an existing plugin

Provide the default set of configuration properties that match your current Gradle plugin project 
setup and let the plugindev-plugin derive the values for the remaining attributes. The complete 
set of configuration properties is shown and explained further down.  

```groovy
plugindev {
    pluginId = 'org.example.foo'
    pluginImplementationClass 'org.example.gradle.foo.FooPlugin'
    pluginDescription 'Gradle plugin that does foo.'
    pluginLicenses 'Apache-2.0'
    pluginTags 'gradle', 'plugin', 'foo'
    authorId 'etiennestuder'
    authorName 'Etienne Studer'
    authorEmail 'etienne@example.org'
    projectUrl 'https://github.com/etiennestuder/gradle-foo-plugin'
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

## Run bintray task

Run the `bintray` Gradle task which will build and upload the Gradle plugin artifacts. Use 
the `-i` option to get more detailed feedback about the bundling and uploading process. 

```console
gradle bintray
```

You can find the complete example [here](example/build.gradle).

# Customization

# Introduction on how to create and publish a Gradle plugin

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License
This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
