package nu.studer.gradle.plugindev

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import java.lang.management.ManagementFactory

abstract class BaseFuncTest extends Specification {

    @Shared
    File testKitDir

    void setupSpec() {
        // define the location of testkit, taking into account that multiple test workers might run in parallel
        testKitDir = new File("build/testkit").absoluteFile
        def workerNum = System.getProperty("org.gradle.test.worker")
        if (workerNum) {
            testKitDir = new File(testKitDir, workerNum)
        }
    }

    @TempDir
    File tempDir

    File workspaceDir
    GradleVersion gradleVersion

    void setup() {
        def testFolder = specificationContext.currentIteration.name.replace(':', '.').replace('\'', '')
        workspaceDir = new File(tempDir, testFolder)
        gradleVersion = determineGradleVersion()

        def localBuildCacheDirectory = new File(workspaceDir, 'local-cache')
        settingsFile << """
buildCache {
  local {
    directory '${localBuildCacheDirectory.toURI()}'
  }
}
        """
    }

    protected BuildResult runWithArguments(String... args) {
        GradleRunner.create()
            .withPluginClasspath()
            .withTestKitDir(testKitDir)
            .withProjectDir(workspaceDir)
            .withArguments(args)
            .withGradleVersion(gradleVersion.version)
            .withDebug(isDebuggerAttached())
            .forwardOutput()
            .build()
    }

    protected File getBuildFile() {
        file('build.gradle')
    }

    protected File getSettingsFile() {
        file('settings.gradle')
    }

    protected File file(String path) {
        file(workspaceDir, path)
    }

    protected File file(File dir, String path) {
        def file = new File(dir, path)
        assert file.parentFile.mkdirs() || file.parentFile.directory
        if (file.exists()) {
            assert file.file
        } else {
            assert file.createNewFile()
        }
        file
    }

    protected static GradleVersion determineGradleVersion() {
        def injectedGradleVersionString = System.getProperty('testContext.gradleVersion')
        injectedGradleVersionString ? GradleVersion.version(injectedGradleVersionString) : GradleVersion.current()
    }

    protected static boolean isDebuggerAttached() {
        ManagementFactory.runtimeMXBean.inputArguments.toString().indexOf("-agentlib:jdwp") > 0
    }

}
