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

package org.vividus.xray.reader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.util.json.JsonUtils;

public final class JsonResourceReader
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResourceReader.class);

    private static final JsonUtils JSON_UTILS = new JsonUtils();

    private JsonResourceReader()
    {
    }

    public static List<FileEntry> readFrom(Path sourceDirectory) throws IOException
    {
        LOGGER.atInfo().addArgument(sourceDirectory).log("Reading JSON files from filesystem by path {}");

        List<FileEntry> fileEntries = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceDirectory))
        {
            for (Path path : paths.filter(Files::isRegularFile).collect(Collectors.toList()))
            {
                File file = path.toFile();
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                boolean json = JSON_UTILS.isJson(content);
                if (!json)
                {
                    LOGGER.atInfo().addArgument(file::getAbsolutePath).log("Content of file '{}' is not JSON");
                    continue;
                }
                fileEntries.add(new FileEntry(file.getAbsolutePath(), content));
            }
        }
        return fileEntries;
    }

    public static final class FileEntry
    {
        private final String path;
        private final String content;

        private FileEntry(String path, String content)
        {
            this.path = path;
            this.content = content;
        }

        public String getPath()
        {
            return path;
        }

        public String getContent()
        {
            return content;
        }
    }
}
