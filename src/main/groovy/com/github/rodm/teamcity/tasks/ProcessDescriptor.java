/*
 * Copyright 2015 Rod MacKenzie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rodm.teamcity.tasks;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.util.Collections;
import java.util.LinkedHashMap;

@CacheableTask
public class ProcessDescriptor extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    private final RegularFileProperty descriptor = getProject().getObjects().fileProperty();

    @Input
    private final MapProperty<String, Object> tokens = getProject().getObjects().mapProperty(String.class, Object.class).convention(new LinkedHashMap<>());

    @OutputFile
    private final RegularFileProperty destination = getProject().getObjects().fileProperty();

    public ProcessDescriptor() {
        setDescription("Processes the plugin descriptor");
        onlyIf(task -> getDescriptor().isPresent());
    }

    public final RegularFileProperty getDescriptor() {
        return descriptor;
    }

    public final MapProperty<String, Object> getTokens() {
        return tokens;
    }

    public final RegularFileProperty getDestination() {
        return destination;
    }

    @TaskAction
    public void process() {
        getProject().copy(copySpec -> {
            copySpec.into(getDestination().get().getAsFile().getParentFile());
            copySpec.from(getDescriptor().get().getAsFile());
            copySpec.rename(name -> getDestination().get().getAsFile().getName());
            if (!tokens.get().isEmpty()) {
                copySpec.filter(Collections.singletonMap("tokens", getTokens().get()), ReplaceTokens.class);
            }
        });
    }
}
