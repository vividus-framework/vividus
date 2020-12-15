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

package org.vividus.bdd.priority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MetaBasedStoryExecutionPriorityTests
{
    private static final String MORROWIND_STORY = "morrowind.story";
    private static final String OBLIVION_STORY = "oblivion.story";
    private static final String SKYRIM_STORY = "skyrim.story";
    private static final String PRIORITY_KEY = "priority_key";

    @Test
    void shouldSortStoriesByNumericValue()
    {
        Story oblivionStory = create(OBLIVION_STORY, Map.of(PRIORITY_KEY, "10"));
        Story morrowindStory = create(MORROWIND_STORY, Map.of());
        Story skyrimStory = create(SKYRIM_STORY, Map.of(PRIORITY_KEY, "1"));

        assertEquals(
            List.of(oblivionStory, skyrimStory, morrowindStory),
            applySort(PRIORITY_KEY, List.of(oblivionStory, morrowindStory, skyrimStory))
        );
    }

    @ValueSource(strings = { "not a number", StringUtils.EMPTY })
    @ParameterizedTest
    void shouldNotSortStoriesIfMetaValueIsNotANumber(String value)
    {
        Story oblivionStory = create(OBLIVION_STORY, Map.of(PRIORITY_KEY, value));
        List<Story> stories = List.of(oblivionStory, oblivionStory);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> applySort(PRIORITY_KEY, stories));
        assertEquals("The meta value is expected to be a number, but got '" + value + '\'', exception.getMessage());
    }

    @ValueSource(strings = { StringUtils.EMPTY, StringUtils.SPACE, "What is it, outlander?" })
    @ParameterizedTest
    void shouldNotCreateComparatorIfMetaNameIsInvalid(String metaName)
    {
        List<Story> stories = List.of();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> applySort(StringUtils.EMPTY, stories));
        assertEquals("The meta name must be a non-zero length string without spaces", exception.getMessage());
    }

    private static List<Story> applySort(String key, List<Story> stories)
    {
        return stories.stream()
                      .sorted(MetaBasedStoryExecutionPriority.byNumericMetaValue(key))
                      .collect(Collectors.toList());
    }

    private static Story create(String name, Map<String, String> metaValues)
    {
        Story story = mock(Story.class);
        when(story.getName()).thenReturn(name);
        Properties props = new Properties();
        props.putAll(metaValues);
        Meta meta = new Meta(props);
        when(story.getMeta()).thenReturn(meta);
        return story;
    }
}
