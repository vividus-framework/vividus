/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.report.allure.plugin;

import static org.apache.commons.lang3.Validate.isTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.vividus.util.ResourceUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.plugin.DefaultPlugin;

public class DynamicPlugin extends DefaultPlugin
{
    private static final String JS_EXTENSION = ".js";
    private static final String CSS_EXTENSION = ".css";

    private final Map<String, Path> pluginFiles = new HashMap<>();

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public DynamicPlugin(String pluginId, String fileToGenerate, Supplier<List<String>> fileLinesSupplier)
            throws IOException
    {
        super(new PluginConfiguration()
                        .setId(pluginId)
                        .setJsFiles(List.of())
                        .setCssFiles(List.of()),
                List.of(), null);

        List<String> fileLines = fileLinesSupplier.get();

        if (!fileLines.isEmpty())
        {
            isTrue(StringUtils.endsWithAny(fileToGenerate, JS_EXTENSION, CSS_EXTENSION),
                    "Generation of only JavaScript and CSS files is supported.");

            Path tempFile = ResourceUtils.createTempFile(fileToGenerate);
            List<String> extensionSpecificLines = isJsFile(fileToGenerate)
                    ? List.of("'use strict';") : List.of();
            Files.write(tempFile, Stream.of(extensionSpecificLines, fileLines).flatMap(List::stream).toList());

            /*
              For UNIX like operating systems default access for temp files is 600, whereas for regular files the
              default access is 644, so the following fix is used to align access bits across all files being created
              during test execution and avoid potential access related issues.
             */
            if (SystemUtils.IS_OS_UNIX)
            {
                Files.setPosixFilePermissions(tempFile,
                        Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ,
                                PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ));
            }
            pluginFiles.put(fileToGenerate, Path.of(tempFile.toUri()));
            Consumer<List<String>> fileConsumer = isJsFile(fileToGenerate)
                    ? f -> getConfig().setJsFiles(f) : f -> getConfig().setCssFiles(f);
            fileConsumer.accept(List.of(fileToGenerate));
        }
    }

    private boolean isJsFile(String fileToGenerate)
    {
        return fileToGenerate.endsWith(JS_EXTENSION);
    }

    @Override
    public Map<String, Path> getPluginFiles()
    {
        return pluginFiles;
    }
}
