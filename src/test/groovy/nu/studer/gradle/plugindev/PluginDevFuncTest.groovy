package nu.studer.gradle.plugindev

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.Unroll

@Unroll
class PluginDevFuncTest extends BaseFuncTest {

    void "can apply plugin"() {
        given:
        settingsFile()
        buildFile()
        examplePlugin()

        when:
        def result = runWithArguments('pluginUnderTestMetadata', 'pluginDescriptors', 'validatePlugins', '-i')

        then:
        fileExists('build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class')
        fileExists('build/pluginDescriptors/nu.studer.example.properties')
        fileExists('build/pluginUnderTestMetadata/plugin-under-test-metadata.properties')

        result.task(':compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':pluginUnderTestMetadata').outcome == TaskOutcome.SUCCESS
        result.task(':pluginDescriptors').outcome == TaskOutcome.SUCCESS
        result.task(':validatePlugins').outcome == TaskOutcome.SUCCESS
    }

    void "can apply plugin with Gradle configuration cache enabled"() {
        given:
        settingsFile()
        buildFile()
        examplePlugin()

        when:
        def result = runWithArguments('pluginUnderTestMetadata', 'pluginDescriptors', 'validatePlugins', 'jar', '--configuration-cache', '-i')

        then:
        fileExists('build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class')
        fileExists('build/pluginDescriptors/nu.studer.example.properties')
        fileExists('build/pluginUnderTestMetadata/plugin-under-test-metadata.properties')

        result.output.contains('Calculating task graph as no configuration cache is available')
        result.task(':validatePlugins').outcome == TaskOutcome.SUCCESS

        when:
        new File(workspaceDir, 'build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class').delete()
        new File(workspaceDir, 'build/pluginDescriptors/nu.studer.example.properties').delete()
        new File(workspaceDir, 'build/pluginUnderTestMetadata/plugin-under-test-metadata.properties').delete()
        result = runWithArguments('pluginUnderTestMetadata', 'pluginDescriptors', 'validatePlugins', 'jar','--configuration-cache', '-i')

        then:
        fileExists('build/classes/java/main/nu/studer/gradle/example/ExamplePlugin.class')
        fileExists('build/pluginDescriptors/nu.studer.example.properties')
        fileExists('build/pluginUnderTestMetadata/plugin-under-test-metadata.properties')

        result.output.contains('Reusing configuration cache.')
        result.task(':validatePlugins').outcome == TaskOutcome.UP_TO_DATE
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
        fileExists('build/pluginDescriptors/nu.studer.example.properties')
        fileExists('build/pluginUnderTestMetadata/plugin-under-test-metadata.properties')

        result.task(':compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':compileTestJava').outcome == TaskOutcome.NO_SOURCE
        result.task(':pluginUnderTestMetadata').outcome == TaskOutcome.SUCCESS
        result.task(':pluginDescriptors').outcome == TaskOutcome.SUCCESS
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

gradlePlugin {
    plugins {
        pluginDevPlugin {
            id = 'nu.studer.example'
            implementationClass = 'nu.studer.gradle.example.ExamplePlugin'
        }
    }
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
