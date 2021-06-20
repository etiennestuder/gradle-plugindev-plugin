package org.example.gradle.foo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FooPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FooPlugin.class);

    @Override
    public void apply(Project target) {
        LOGGER.info("Applied FooPlugin.");
    }

}
