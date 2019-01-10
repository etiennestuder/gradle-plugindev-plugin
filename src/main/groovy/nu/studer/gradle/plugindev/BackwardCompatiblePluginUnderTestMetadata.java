package nu.studer.gradle.plugindev;

import nu.studer.java.util.OrderedProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.gradle.util.CollectionUtils.collect;

@SuppressWarnings({"UnstableApiUsage", "WeakerAccess"})
public class BackwardCompatiblePluginUnderTestMetadata extends DefaultTask {

    private FileCollection pluginClasspath;
    private File outputDirectory;

    @Input
    protected List<String> getPaths() {
        Iterable<File> classpathFiles = getPluginClasspath() != null ? getPluginClasspath() : emptyList();
        return collect(classpathFiles, file -> file.getAbsolutePath().replaceAll("\\\\", "/"));
    }

    @Classpath
    public FileCollection getPluginClasspath() {
        return pluginClasspath;
    }

    public void setPluginClasspath(FileCollection pluginClasspath) {
        this.pluginClasspath = pluginClasspath;
    }

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @TaskAction
    public void generate() {
        OrderedProperties properties = new OrderedProperties.OrderedPropertiesBuilder().withSuppressDateInComment(true).build();

        List<String> paths = getPaths();
        if (!paths.isEmpty()) {
            String implementationClasspath = join(paths);
            properties.setProperty(PluginUnderTestMetadata.IMPLEMENTATION_CLASSPATH_PROP_KEY, implementationClasspath);
        }

        File outputFile = new File(getOutputDirectory(), PluginUnderTestMetadata.METADATA_FILE_NAME);
        saveProperties(properties, outputFile);
    }

    private static String join(Iterable<String> elements) {
        StringBuilder result = new StringBuilder();
        Iterator<String> parts = elements.iterator();
        if (parts.hasNext()) {
            result.append(parts.next());
            while (parts.hasNext()) {
                result.append(File.pathSeparator);
                result.append(parts.next());
            }
        }
        return result.toString();
    }

    private static void saveProperties(OrderedProperties properties, File outputFile) {
        try {
            try (FileOutputStream propertiesFileOutputStream = new FileOutputStream(outputFile)) {
                properties.store(propertiesFileOutputStream, null);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
