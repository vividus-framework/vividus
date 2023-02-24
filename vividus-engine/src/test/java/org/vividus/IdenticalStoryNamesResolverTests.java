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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class IdenticalStoryNamesResolverTests
{
    private static final String SEPARATOR = "/";
    private static final String DIRECTORY = "directory";
    private static final String NAME = "name.story";
    private static final String ANOTHER_NAME = "nomen.story";

    private static final IdenticalStoryNamesResolver NAMES_RESOLVER = new IdenticalStoryNamesResolver();

    @Test
    void shouldResolveIdenticalNames()
    {
        Story storyName = new Story("file:/tests/name.story");
        Story storyDirectoryName = new Story("file:/tests/directory/name.story");
        Story storyDirectoryAnotherName = new Story("file:/tests/directory/nomen.story");
        Story storyDirectoryDirectoryName = new Story("file:/tests/directory/directory/name.story");
        Story storyDirectory1Name = new Story("file:/tests/directory1/name.story");
        List<Story> batchStories = List.of(storyName, storyDirectoryName, storyDirectoryAnotherName,
                storyDirectoryDirectoryName, storyDirectory1Name);

        NAMES_RESOLVER.resolveIdenticalNames(batchStories);
        assertAll(
                () -> assertEquals(NAME, storyName.getName()),
                () -> assertEquals(DIRECTORY + SEPARATOR + NAME, storyDirectoryName.getName()),
                () -> assertEquals(ANOTHER_NAME, storyDirectoryAnotherName.getName()),
                () -> assertEquals(DIRECTORY + SEPARATOR + DIRECTORY + SEPARATOR + NAME,
                        storyDirectoryDirectoryName.getName()),
                () -> assertEquals("directory1" + SEPARATOR + NAME, storyDirectory1Name.getName())
        );
    }

    @Test
    void shouldResolveIdenticalNamesWindows()
    {
        String namePath = "file:/C:/tests/name.story";
        String directoryNamePath = "file:/C:/tests/directory/name.story";

        Story storyName = new Story(namePath);
        Story storyDirectoryName = new Story(directoryNamePath);
        List<Story> batchStories = List.of(storyName, storyDirectoryName);

        URI nameStoryFullPathUri = URI.create("file:/C:/tests/");
        Path nameStoryResultPath = Paths.get(nameStoryFullPathUri);
        Path nameStoryResultPathFull = Paths.get(nameStoryResultPath.toString(), NAME);

        URI directoryNameStoryFullPathUri = URI.create("file:/C:/tests/directory/");
        Path directoryNameStoryResultPath = Paths.get(directoryNameStoryFullPathUri);
        Path directoryNameStoryResultPathFull = Paths.get(directoryNameStoryResultPath.toString(), NAME);

        try (MockedStatic<Paths> paths = mockStatic(Paths.class))
        {
            paths.when(() -> Paths.get(namePath)).thenThrow(InvalidPathException.class);
            paths.when(() -> Paths.get(nameStoryFullPathUri)).thenReturn(nameStoryResultPath);
            paths.when(() -> Paths.get(nameStoryResultPath.toString(), NAME)).thenReturn(nameStoryResultPathFull);

            paths.when(() -> Paths.get(directoryNamePath)).thenThrow(InvalidPathException.class);
            paths.when(() -> Paths.get(directoryNameStoryFullPathUri)).thenReturn(directoryNameStoryResultPath);
            paths.when(() -> Paths.get(directoryNameStoryResultPath.toString(), NAME))
                    .thenReturn(directoryNameStoryResultPathFull);

            NAMES_RESOLVER.resolveIdenticalNames(batchStories);
        }
        assertAll(
                () -> assertEquals(NAME, storyName.getName()),
                () -> assertEquals(DIRECTORY + SEPARATOR + NAME, storyDirectoryName.getName())
        );
    }

    @Test
    void shouldNotProcessDifferentNames()
    {
        Story storyMock1 = mock();
        Story storyMock2 = mock();
        when(storyMock1.getPath()).thenReturn("file:/tests/name1.story");
        when(storyMock2.getPath()).thenReturn("file:/tests/name2.story");
        when(storyMock1.getName()).thenReturn("name1");
        when(storyMock2.getName()).thenReturn("name2");
        List<Story> batchStories = List.of(storyMock1, storyMock2);

        NAMES_RESOLVER.resolveIdenticalNames(batchStories);
        verify(storyMock1, times(3)).getName();
        verify(storyMock2, times(3)).getName();
        verifyNoMoreInteractions(storyMock1, storyMock2);
    }
}
