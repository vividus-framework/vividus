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

package org.vividus.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToSimpleName;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.Test;

class ExtendedStoryReporterBuilderTests
{
    private final ExtendedStoryReporterBuilder builder = new ExtendedStoryReporterBuilder();

    @Test
    void testSetFormats()
    {
        List<Format> formats = List.of(Format.XML);
        builder.setFormats(formats);
        assertEquals(List.of(org.jbehave.core.reporters.Format.XML), builder.formats());
    }

    @Test
    void testSetNullFormats()
    {
        builder.setFormats(null);
        assertEquals(List.of(), builder.formats());
    }

    @Test
    void testSetPathResolver()
    {
        ResolveToSimpleName resolveToSimpleName = new ResolveToSimpleName();
        builder.setPathResolver(resolveToSimpleName);
        assertEquals(resolveToSimpleName, builder.pathResolver());
    }

    @Test
    void testBuild()
    {
        String storyPath = "path";
        StoryReporter reporter = builder.build(storyPath);
        assertEquals(reporter, builder.build(storyPath));
    }
}
