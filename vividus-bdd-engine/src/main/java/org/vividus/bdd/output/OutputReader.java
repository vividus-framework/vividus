/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.output;

import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.model.jbehave.Story;
import org.vividus.bdd.output.JsonResourceReader.FileEntry;

public final class OutputReader
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputReader.class);

    private OutputReader()
    {
    }

    public static List<Story> readStoriesFromJsons(Path jsonDirectory) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        List<Story> stories = new ArrayList<>();
        for (FileEntry jsonResult : getJsonResultsFiles(jsonDirectory))
        {
            LOGGER.atInfo().addArgument(jsonResult::getPath).log("Parsing {}");
            stories.add(objectMapper.readValue(jsonResult.getContent(), Story.class));
        }
        return stories;
    }

    private static List<FileEntry> getJsonResultsFiles(Path jsonDirectory) throws IOException
    {
        List<FileEntry> jsonFiles = JsonResourceReader.readFrom(jsonDirectory);

        notEmpty(jsonFiles, "The directory '%s' does not contain needed JSON files", jsonDirectory);
        LOGGER.atInfo().addArgument(() -> jsonFiles.stream().map(FileEntry::getPath).collect(Collectors.joining(", ")))
                       .log("JSON files: {}");
        return jsonFiles;
    }
}
