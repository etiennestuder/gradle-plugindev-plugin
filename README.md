gradle-plugindev-plugin
=======================

# Overview
[Gradle](http://www.gradle.org) plugin that facilitates the bundling and uploading 
of Gradle plugins as expected by the Gradle Plugin Portal, JCenter, and MavenCentral.

# High-level goals
The gradle-plugindev plugin takes care of creating the artifacts of a Gradle plugin and of uploading these to Bintray. The 
following high-level goals are driving the functionality of the gradle-plugindev plugin: 

 * All boiler-plate configuration to bundle and upload the Gradle plugin artifacts must be avoided
 * Compliance of the Gradle plugin artifacts with the Gradle Plugin Portal, JCenter, and MavenCentral requirements must be ensured
 * All bundling and uploading configuration must happen in one place and without any redundancy
 * High consistency between the representation of different plugins should be achieved
 * Customization of the provided functionality should be possible 

# Functionality

The following functionality is provided by the gradle-plugindev plugin:
 
 * Applies the Java plugin to the project
 * Includes JCenter as a repository for dependency resolution
 * Adds the Gradle API to the 'compile' configuration
 * Sets the compiler source and target compatibility to 1.6, the minimum version supported by Gradle
 * Adds a task that puts all main Java and/or Groovy sources into a Jar file
 * Adds a task that puts all Javadoc and/or Groovydoc into a Jar file
 * Adds a task that generates the plugin descriptor file
 * Includes the generated plugin descriptor file in the production Jar file
 * Validates that the plugin implementation class declared in the plugin descriptor file is contained in the production Jar file
 * Adds a manifest file with meta data about the plugin to each Jar file
 * Adds a license file to each Jar file if available in the root of the project
 * Creates a POM file with the required metadata
 * Creates a publication with the production Jar file, sources Jar file, documentation Jar file, and the POM file
 * Publishes the bundled plugin artifacts to Bintray as a new version and optionally in a new package
 * Ensures the published version has the required Bintray attributes set

# Configuration

## Publishing a new plugin

Apply the gradle-plugindev plugin in your Gradle project:

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

Make sure to also apply the 'groovy' plugin if you intend to write your plugin in Groovy.

Configure the gradle-plugindev plugin through the `plugindev` configuration block.  

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

The complete set of configuration properties is shown and explained further down.

Provide the remaining bintray configuration through the `bintray` configuration block.

```groovy
bintray {
    user = "$BINTRAY_USER"
    key = "$BINTRAY_API_KEY"
    pkg.repo = 'gradle-plugins'
}
```

A good place to store the bintray credentials is the gradle.properties file in your Gradle user home directory.

Run the `bintray` Gradle task and the Gradle plugin artifacts are built and uploaded. 

```console
gradle bintray
```

Use `gradle bintray -i` to get more detailed feedback about the bundling and uploading.
 
## Publishing an existing plugin

```groovy
plugindev {
    pluginId = 'org.example.foo'
    pluginDescription 'Gradle plugin that does foo.'
    pluginImplementationClass 'org.example.gradle.foo.FooPlugin'
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

## Customization

# Introduction on how to create and publish a Gradle plugin

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License
This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
