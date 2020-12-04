/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.zephyr.configuration;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ZephyrFileVisitor extends SimpleFileVisitor<Path>
{
    private static final String JSON_FILE_SUFFIX = ".json";
    private static final String TEST_CASES_DIRECTORY = "test-cases";

    private final List<File> files = new ArrayList<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
        String path = file.toString();
        if (file.getFileName().toString().endsWith(JSON_FILE_SUFFIX) && path.contains(TEST_CASES_DIRECTORY))
        {
            files.add(file.toFile());
        }
        return CONTINUE;
    }

    public List<File> getFiles()
    {
        return files;
    }
}
