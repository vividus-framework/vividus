/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus;

import java.io.File;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.model.Story;

public class IdenticalStoryNamesResolver
{
    private static final char UNIX_PATH_SEPARATOR = '/';

    public void resolveIdenticalNames(List<Story> stories)
    {
        for (Story story : stories)
        {
            String storyName = story.getName();
            List<Story> sameNameStories = stories.stream()
                    .filter(s -> storyName.equals(s.getName()))
                    .collect(Collectors.toList());

            if (sameNameStories.size() > 1)
            {
                Path currentPath = getPathSafely(story.getPath());
                List<Path> similarPaths = sameNameStories.stream()
                        .map(s -> getPathSafely(s.getPath()))
                        .collect(Collectors.toList());

                Path commonPath = findCommonPath(currentPath, similarPaths);
                updateStoryNames(commonPath, sameNameStories);
            }
        }
    }

    private Path findCommonPath(Path referencePath, List<Path> paths)
    {
        Path root = referencePath.getRoot();
        Path commonPath = root != null ? root : Paths.get("");

        for (Path directory : referencePath)
        {
            Path possibleCommonPath = commonPath.resolve(directory);
            if (paths.stream().anyMatch(path -> !path.startsWith(possibleCommonPath)))
            {
                break;
            }
            commonPath = possibleCommonPath;
        }
        return commonPath;
    }

    private void updateStoryNames(Path commonPath, List<Story> stories)
    {
        for (Story story : stories)
        {
            String prefix = getPrefix(commonPath, getPathSafely(story.getPath()));
            if (prefix != null)
            {
                if (File.separatorChar == '\\')
                {
                    prefix = prefix.replace(File.separatorChar, UNIX_PATH_SEPARATOR);
                }
                story.namedAs(prefix + UNIX_PATH_SEPARATOR + story.getName());
            }
        }
    }

    private String getPrefix(Path commonPath, Path storyPath)
    {
        Path prefix = commonPath.relativize(storyPath).getParent();
        return prefix != null ? prefix.toString() : null;
    }

    private Path getPathSafely(String path)
    {
        try
        {
            return Paths.get(path);
        }
        catch (InvalidPathException e)
        {
            Path resultPath = Paths.get(URI.create(FilenameUtils.getFullPath(path)));
            return Paths.get(resultPath.toString(), FilenameUtils.getName(path));
        }
    }
}
