<p align="left">
  <a href="https://github.com/etiennestuder/gradle-plugindev-plugin/actions?query=workflow%3A%22Build+Gradle+project%22"><img src="https://github.com/etiennestuder/gradle-plugindev-plugin/workflows/Build%20Gradle%20project/badge.svg"></a>
</p>

gradle-plugindev-plugin
=======================

> The work on this software project is in no way associated with my employer nor with the role I'm having at my employer. Any requests for changes will be decided upon exclusively by myself based on my personal preferences. I maintain this project as much or as little as my spare time permits.

# Important

The [Plugin Publishing Plugin](https://plugins.gradle.org/docs/publish-plugin) provided by [Gradle Inc.](http://gradle.org)
allows you to upload 3rd-party Gradle plugins directly to the [Gradle Plugins Portal](https://plugins.gradle.org). The process
is described in detail [here](https://plugins.gradle.org/docs/submit).

Hosting of 3rd-party Gradle plugins via Bintray is no longer supported by JFrog.

# Overview

[Gradle](http://www.gradle.org) plugin that facilitates the bundling and publishing
of Gradle plugins as expected by the [Gradle Plugin Portal](http://plugins.gradle.org/).

The plugin further ensures that all requirements for inclusion in the Gradle Plugin Portal are met.

The plugindev plugin is hosted at the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/nu.studer.plugindev).

## Build scan

Recent build scan: https://gradle.com/s/bzxoqt2io4epw

Find out more about build scans for Gradle and Maven at https://scans.gradle.com.

# Goals

The plugindev plugin takes care of creating the artifacts of a Gradle plugin and of publishing these to the Plugin Portal. The
following high-level goals are driving the functionality of the plugindev plugin:

 * Compliance of the Gradle plugin artifacts with the Plugin Portal must be ensured
 * All boiler-plate configuration to bundle and publish the Gradle plugin artifacts should be avoided
 * All bundle and publish configuration must happen without redundancy
 * Customization of the provided functionality should be possible
 * High consistency between the representation of different plugins should be achieved
 * Functionality provided by existing plugins and Gradle should be reused as much as possible

# Functionality

The following functionality is provided by the plugindev plugin:

 * Applies the Java plugin to the project
 * Includes MavenCentral as a repository for dependency resolution
 * Adds the Gradle API to the 'compileOnly' configuration
 * Sets the compiler source and target compatibility to 1.8, minimum version supported by Gradle
 * Adds a task that puts all main Java and/or Groovy sources into a Jar file
 * Adds a task that puts all Javadoc and/or Groovydoc into a Jar file
 * Adds a task that generates the plugin descriptor file
 * Includes the generated plugin descriptor file in the production Jar file
 * Validates the plugin implementation class declared in the plugin descriptor file is contained in the production Jar file
 * Adds a manifest file to each Jar file with metadata about the plugin
 * Adds a license file to each Jar file if available in the root of the project
 * Creates a POM file with the required metadata derived from the plugin configuration
 * Creates a publication with the production Jar file, sources Jar file, documentation Jar file, and the POM file
 * Publishes the bundled plugin artifacts to the Plugin Portal

# Design

The plugindev plugin creates all the required artifacts through Gradle core tasks. The configuration of these
artifacts happens in a central place through the `plugindev` extension. The
[MavenPublishPlugin](http://www.gradle.org/docs/current/userguide/publishing_maven.html) is leveraged to create
a publication from these artifacts.

The configuration of the metadata at the publication destination (PluginPortal) happens through the `plugindev`
extension. The [Plugin Publishing Plugin](https://plugins.gradle.org/docs/publish-plugin) is leveraged
to publish the publication to Gradle Plugin Portal.

# High-level steps

The following were the high-level steps to get your new plugin into the [Gradle Plugin Portal](http://plugins.gradle.org/)
when using the plugindev plugin:

1. Apply and configure the plugindev plugin to your Gradle project as explained [below](#configuration)
1. Request inclusion of your plugin in the [Gradle Plugin Portal](http://plugins.gradle.org/)
1. Wait a few minutes for your plugin to appear in the Gradle Plugin Portal

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
  id 'nu.studer.plugindev' version '1.0.12'
}
```

## Set group and version

Set the `group` and `version` of your Gradle plugin project. Both the `group` and `version` have to be defined _before_ the `plugindev` extension.

```groovy
// make sure to define the group and version before the 'plugindev' extension
group = 'org.example'
version = '0.0.1.DEV'
```

## Declare external dependencies

Declare the external dependencies of your Gradle plugin project, if any. The external dependencies have to be defined _before_ the `plugindev` extension
in order to show up properly in the _pom.xml_ that is generated by Gradle.

```groovy
// make sure to define the external dependencies before the 'plugindev' extension
dependencies {
  implementation 'nu.studer:java-ordered-properties:1.0.4'
  testImplementation 'org.spockframework:spock-core:2.0-groovy-2.5'
}
```

## Configure plugindev plugin

Configure the plugindev plugin through the `plugindev` extension.

### When building and publishing a new plugin

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
    done() // do not omit this
}
```

In the example above, it is assumed that the plugin is hosted on [GitHub](https://github.com/). Thus,
the configuration properties for the issue url and the vcs url are automatically derived by the
plugindev plugin.

### When building and publishing an existing plugin

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
    done() // do not omit this
}
```

In the example above, no assumptions are made about where your project is hosted. Thus,
the configuration properties for the issue url and the vcs url must be declared explicitly.

## Run publishPluginPublicationToMavenLocal task

Run the `publishPluginPublicationToMavenLocal` Gradle task which will build the plugin artifacts and publish them
to your local Maven repository. Use the `-i` option to get more detailed feedback about the bundling and publishing progress.

```console
gradle publishPluginPublicationToMavenLocal
```

## Complete example

The previously explained configuration steps lead to the following functional build file, applying
the minimal set of required configuration properties.

If you want or need a different plugin id than the one automatically derived from the plugin implementation class, you
also have to specify the ```pluginId```.

If your plugin is not hosted on GitHub, you also have to specify the ```projectIssuesUrl``` and the ```projectVcsUrl```.

```groovy
plugins {
    id 'nu.studer.plugindev' version '1.0.12'
}

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
    done() // do not omit this
}
```

You can find the complete [example](example/minimal_config/build.gradle) on GitHub.

## Configuration reference

The following configuration contains the complete set of configuration properties.

```groovy
plugindev {
    pluginId 'org.example.foo'
    pluginName 'gradle-foo-plugin'
    pluginImplementationClass 'org.example.gradle.foo.FooPlugin'
    pluginDescription 'Gradle plugin that does foo.'
    pluginLicenses 'Apache-2.0'
    pluginTags 'gradle', 'plugin', 'foo'
    authorId 'homer'
    authorName 'Homer Simpson'
    authorEmail 'homer@simpson.org'
    projectUrl 'https://github.com/homer/gradle-foo-plugin'
    projectIssuesUrl 'https://github.com/homer/gradle-foo-plugin/issues'
    projectVcsUrl 'https://github.com/homer/gradle-foo-plugin.git'
    projectInceptionYear '2014'
    pomConfiguration {
        name 'gradle-foo-plugin'
        description 'Gradle plugin that does foo.'
        url 'https://github.com/homer/gradle-foo-plugin'
        inceptionYear '2014'
        ...
    }
    done() // do not omit this
}
```

You can find the complete [example](example/full_config/build.gradle) on GitHub.

### pluginId

The id of the plugin. The id is how your plugin will be referenced in Gradle builds, e.g.
through *apply plugin: 'thePluginId'*.

If not set explicitly, the id is derived from
the `pluginImplementationClass` property by taking the package name of that class and
removing any occurrences of *gradle* and *plugin* package names, e.g.
for *org.example.gradle.foo.FooPlugin* the derived plugin id is *org.example.foo*.

### pluginName

The name of the plugin. The name shows up in the manifest files of the Jar files.

If not set explicitly, the plugin name is derived from the name of the containing Gradle project. By default,
Gradle derives the name of the project from the containing folder. A custom project name could be set during
the initialization phase in the settings.gradle file.

### pluginImplementationClass

The entry point of your plugin. A reference to the class that implements `org.gradle.api.Plugin`.

### pluginDescription

The full-text description of the plugin. The description shows up in the POM file and in the Gradle Plugin Portal.

### pluginLicenses

The license(s) under which the plugin is available. Multiple licenses can be specified as a comma-separated list. The specified
licenses show up in the POM file with their full name. Currently, the plugindev plugin can handle the
Apache (`Apache-2.0`), GPL (`GPL-3.0`, `GPL-2.0`, `GPL-1.0`), LGPL (`LGPL-3.0`, `LGPL-2.1`), and MIT (`MIT`) licenses.

### pluginTags

The tags of your plugin. Multiple tags can be specified as a comma-separated list. The tags show up in the Gradle Plugin Portal.

### authorId

The plugin author's id. The author id shows up in the POM file.

### authorName

The plugin author's name. The author name shows up in the POM file.

### authorEmail

The plugin author's email address. The author name shows up in the POM file.

### projectUrl

The location where the project is hosted.

### projectIssuesUrl

The location of the project's issue management.

If not set explicitly and the `projectUrl` points to 'https://github.com/...', the project's GitHub
issue management url will be set.

### projectVcsUrl

The location of the project's version control.

If not set explicitly and the `projectUrl` points to 'https://github.com/...', the project's GitHub
vcs url will be set.

### projectInceptionYear

The inception year of the plugin.

### pomConfiguration

The closure to apply to populate the POM file.

If not set explicitly, the POM file is enriched with the values defined through the plugindev configuration properties.

# Customization

At least the following customizations are possible:

 * change the source and target compatibility version
 * add more sources and more documentation to the generated artifacts
 * include additional artifacts in the plugin publication

The documentation will be enhanced with more details regarding customization upon request.

# Changelog
+ 3.0 - Change from publishing to Bintray to publishing to the Plugin Portal directly
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
