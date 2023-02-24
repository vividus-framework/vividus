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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunTestContext;

@ExtendWith(MockitoExtension.class)
class ResolveToUniqueSimpleNameTests
{
    private static final String EXTENSION = "xml";
    private static final String SEPARATOR = File.separator;
    private static final String BATCH_DIR = "batch-1";
    private static final String STORY_NAME = "file:/tests/name.story";
    private static final String STORY_DIRECTORY_NAME = "file:/tests/directory/name.story";
    private static final String STORY_DIRECTORY_ANOTHER_NAME = "file:/tests/directory/nomen.story";
    private static final String STORY_DIRECTORY_DIRECTORY_NAME = "file:/tests/directory/directory/name.story";
    private static final String STORY_DIRECTORY1_NAME = "file:/tests/directory1/name.story";

    private static final BatchedPerformableTree PERFORMABLE_TREE = new BatchedPerformableTree();
    private static final ResolveToUniqueSimpleName RESOLVE_TO_UNIQUE_SIMPLE_NAME = new ResolveToUniqueSimpleName(
            new RunTestContext(), PERFORMABLE_TREE);

    private URL codeLocation;

    @BeforeEach
    void beforeEach() throws MalformedURLException
    {
        codeLocation = new URL("file:/location/");
        PerformableRoot root = PERFORMABLE_TREE.getRoot();
        root.add(createPerformableStory(new Story(STORY_NAME)));
        root.add(createPerformableStory(new Story(STORY_DIRECTORY_NAME)));
        root.add(createPerformableStory(new Story(STORY_DIRECTORY_ANOTHER_NAME)));
        root.add(createPerformableStory(new Story(STORY_DIRECTORY_DIRECTORY_NAME)));
        root.add(createPerformableStory(new Story(STORY_DIRECTORY1_NAME)));
    }

    static Stream<Arguments> storiesDataProvider()
    {
        return Stream.of(
                arguments(STORY_NAME, "name"),
                arguments(STORY_DIRECTORY_NAME, "directory.name"),
                arguments(STORY_DIRECTORY_ANOTHER_NAME, "nomen"),
                arguments(STORY_DIRECTORY_DIRECTORY_NAME, "directory.directory.name"),
                arguments(STORY_DIRECTORY1_NAME, "directory1.name")
        );
    }

    @ParameterizedTest
    @MethodSource("storiesDataProvider")
    void shouldResolveName(String storyPath, String expectedName)
    {
        PERFORMABLE_TREE.performBeforeOrAfterStories(null, Stage.BEFORE);
        StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
        assertEquals(String.format("%s%s%s.%s", BATCH_DIR, SEPARATOR, expectedName, EXTENSION),
                RESOLVE_TO_UNIQUE_SIMPLE_NAME.resolveName(storyLocation, EXTENSION));
    }

    @ParameterizedTest
    @ValueSource(strings = { "BeforeStories", "AfterStories" })
    void shouldIgnoreBeforeAndAfterStories(String ignoredPath)
    {
        PerformableTree performableTreeMocked = mock();
        ResolveToUniqueSimpleName resolveToUniqueSimpleName = new ResolveToUniqueSimpleName(new RunTestContext(),
                performableTreeMocked);
        StoryLocation storyLocation = new StoryLocation(codeLocation, ignoredPath);

        resolveToUniqueSimpleName.resolveName(storyLocation, EXTENSION);
        verifyNoInteractions(performableTreeMocked);
    }

    private PerformableStory createPerformableStory(Story story)
    {
        return new PerformableStory(story, null, false);
    }
}
