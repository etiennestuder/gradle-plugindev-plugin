package nu.studer.gradle.plugindev

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.PendingFeature
import spock.lang.Unroll

@Unroll
class PluginDevFuncTest extends BaseFuncTest {

    void "can apply plugin"() {
        given:
        settingsFile()
        buildFile()
        examplePlugin()

        when:
        def result = runWithArguments('publishPluginToBintray', '-i')

        then:
        fileExists('build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class')
        fileExists('build/plugindev/generated-resources/main/META-INF/gradle-plugins/nu.studer.example.properties')
        result.task(':pluginDescriptorFile').outcome == TaskOutcome.SUCCESS
        result.task(':compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':sourcesJar').outcome == TaskOutcome.SUCCESS
        result.task(':docsJar').outcome == TaskOutcome.SUCCESS
        result.task(':jar').outcome == TaskOutcome.SUCCESS
        result.task(':bintrayUpload').outcome == TaskOutcome.SUCCESS
        result.output.contains('Uploading to https://api.bintray.com/content/someUser/gradle-plugins/gradle-example-plugin/0.1/nu/studer/gradle-example-plugin/0.1/gradle-example-plugin-0.1-sources.jar...')
        result.output.contains('Uploading to https://api.bintray.com/content/someUser/gradle-plugins/gradle-example-plugin/0.1/nu/studer/gradle-example-plugin/0.1/gradle-example-plugin-0.1-javadoc.jar...')
        result.output.contains('Uploading to https://api.bintray.com/content/someUser/gradle-plugins/gradle-example-plugin/0.1/nu/studer/gradle-example-plugin/0.1/gradle-example-plugin-0.1.jar...')
        result.output.contains('Uploading to https://api.bintray.com/content/someUser/gradle-plugins/gradle-example-plugin/0.1/nu/studer/gradle-example-plugin/0.1/gradle-example-plugin-0.1.pom...')
    }

    @PendingFeature
    void "can apply plugin with Gradle configuration cache enabled"() {
        given:
        gradleVersion = GradleVersion.version('6.5')
        settingsFile()
        buildFile()
        examplePlugin()

        when:
        def result = runWithArguments('publishPluginToBintray', '--configuration-cache=on', '-i')

        then:
        fileExists('build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class')
        fileExists('build/plugindev/generated-resources/main/META-INF/gradle-plugins/nu.studer.example.properties')
        result.output.contains("Calculating task graph as no configuration cache is available for tasks: publishPluginToBintray")
        result.task(':pluginDescriptorFile').outcome == TaskOutcome.SUCCESS

        when:
        new File(workspaceDir, 'build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class').delete()
        new File(workspaceDir, 'build/plugindev/generated-resources/main/META-INF/gradle-plugins/nu.studer.example.properties').delete()
        result = runWithArguments('publishPluginToBintray', '--configuration-cache=on', '-i')

        then:
        fileExists('build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class')
        fileExists('build/plugindev/generated-resources/main/META-INF/gradle-plugins/nu.studer.example.properties')
        result.output.contains("Reusing configuration cache.")
        result.task(':compileFooRocker').outcome == TaskOutcome.SUCCESS
    }

    void "can run tests"() {
        given:
        settingsFile()
        buildFile()
        examplePlugin()

        when:
        def result = runWithArguments('test', '-i')

        then:
        fileExists('build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class')
        fileExists('build/plugindev/generated-resources/main/META-INF/gradle-plugins/nu.studer.example.properties')
        fileExists('build/pluginUnderTestMetadata/plugin-under-test-metadata.properties')
        result.task(':pluginDescriptorFile').outcome == TaskOutcome.SUCCESS
        result.task(':pluginUnderTestMetadata').outcome == TaskOutcome.SUCCESS
        result.task(':compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':compileTestJava').outcome == TaskOutcome.NO_SOURCE
        result.task(':test').outcome == TaskOutcome.NO_SOURCE
    }

    private File settingsFile() {
        return settingsFile << """

rootProject.name = 'gradle-example-plugin'

"""
    }

    private File buildFile() {
        buildFile << """
plugins {
    id 'nu.studer.plugindev'
}

group = 'nu.studer'
version = '0.1'

plugindev {
    pluginId 'nu.studer.example'
    pluginDescription 'Some description'
    pluginImplementationClass 'nu.studer.gradle.example.ExamplePlugin'
    pluginLicenses 'Apache-2.0'
    pluginTags 'gradle', 'plugin', 'example'
    authorId 'etiennestuder'
    authorName 'Etienne Studer'
    authorEmail 'etienne@studer.nu'
    projectUrl 'https://github.com/etiennestuder/gradle-example-plugin'
    projectInceptionYear '2019'
    done()
}

bintray {
    user = 'someUser'
    key = 'someKey'
    pkg.repo = 'gradle-plugins'
    dryRun = true
}

"""
    }

    private File examplePlugin() {
        return file('src/main/java/nu/studer/gradle/example/ExamplePlugin.java') << """

package nu.studer.gradle.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ExamplePlugin implements Plugin<Project> {
    public void apply(Project project) {
    }
}

"""
    }

    private boolean fileExists(String filePath) {
        def file = new File(workspaceDir, filePath)
        file.exists() && file.file
    }

}
