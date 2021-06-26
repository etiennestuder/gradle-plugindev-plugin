<p align="left">
  <a href="https://github.com/etiennestuder/gradle-plugindev-plugin/actions?query=workflow%3A%22Build+Gradle+project%22"><img src="https://github.com/etiennestuder/gradle-plugindev-plugin/workflows/Build%20Gradle%20project/badge.svg"></a>
</p>

gradle-plugindev-plugin
=======================

> The work on this software project is in no way associated with my employer nor with the role I'm having at my employer. Any requests for changes will be decided upon exclusively by myself based on my personal preferences. I maintain this project as much or as little as my spare time permits.

# Overview

[Gradle](http://www.gradle.org) plugin that facilitates the bundling
of Gradle plugins as expected by the [Gradle Plugin Portal](http://plugins.gradle.org/).

Hosting of 3rd-party Gradle plugins via Bintray is no longer supported by JFrog. As a consequence, the value the
plugindev plugin once used to provide is now highly diminished.

The plugindev plugin is hosted at the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/nu.studer.plugindev).

## Build scan

Recent build scan: https://gradle.com/s/bzxoqt2io4epw

Find out more about build scans for Gradle and Maven at https://scans.gradle.com.

# Functionality

The following functionality is provided by the plugindev plugin:

 * Applies the JavaGradlePluginPlugin plugin to the project
 * Includes MavenCentral as a repository for dependency resolution
 * Sets the compiler source and target compatibility to 1.8
 * Adds a task that puts all main sources into a Jar file
 * Adds a task that puts all Javadoc into a Jar file
 * Adds some build metadata to the jar files' manifest file

# Prerequisites

The following one-time setup must already be present for the plugindev plugin to continue to do its work:

1. You must have a user account with the [Gradle Plugin Portal](http://plugins.gradle.org/)
1. You must have created an API key for uploading to the [Gradle Plugin Portal](http://plugins.gradle.org/)

# Configuration

## Apply plugindev plugin

Apply the `nu.studer.plugindev` plugin to your Gradle plugin project. Make sure to also
apply the `groovy` plugin if you intend to write your plugin in Groovy.

```groovy
plugins {
  id 'nu.studer.plugindev' version '3.0'
}
```

## Set group and version

Set the `group` and `version` of your Gradle plugin project.

```groovy
group = 'org.example'
version = '0.0.1.DEV'
```

## Declare external dependencies

Declare the external dependencies of your Gradle plugin project, if any.

```groovy
dependencies {
  implementation 'nu.studer:java-ordered-properties:1.0.4'
  testImplementation 'org.spockframework:spock-core:2.0-groovy-2.5'
}
```

# Changelog
+ 3.0 - Change from publishing to Bintray to publishing to the Plugin Portal directly.
+ 2.0 - Add compatibility with Gradle 7.0
+ 1.0.12 - Add compatibility all the way back to Gradle 3.5.1.
+ 1.0.11 - Register custom tasks at plugin application time.
+ 1.0.10 - Include plugin under test meta data file to run functional tests with TestKit.
+ 1.0.9 - Set issueManagement.url and license.distribution in pom.xml.
+ 1.0.8 - Set source and target compatibility for Java and Groovy to 1.8.
+ 1.0.7 - Use version 1.8.4 of gradle-bintray-plugin. Build gradle-plugindev-plugin project with Gradle 4.10.3.
+ 1.0.6 - Downgrade to version 1.6 of gradle-bintray-plugin to avoid requirement on Java 8.
+ 1.0.5 - Use version 1.7 of gradle-bintray-plugin. Build gradle-plugindev-plugin project with Gradle 2.14.
+ 1.0.4 - Use version 1.2 of gradle-bintray-plugin.
+ 1.0.3 - Use version 1.0.1 of java-ordered-properties dependency.
+ 1.0.2 - Simplify code by making use of java-ordered-properties dependency.
+ 1.0.1 - Include the plugindev plugin version in the MANIFEST file of the archives.
+ 1.0.0 - Initial public version of the plugindev plugin.

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
