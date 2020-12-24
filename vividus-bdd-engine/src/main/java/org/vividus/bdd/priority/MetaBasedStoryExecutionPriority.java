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

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Comparator;

import org.apache.commons.lang3.math.NumberUtils;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Story;

/**
 * Methods of this class are expected to be used in test projects as static factories for story comparators
 */
public final class MetaBasedStoryExecutionPriority
{
    private MetaBasedStoryExecutionPriority()
    {
    }

    /**
     * Create a comparator that sorts stories by a numeric meta value extracted by the meta name in descending order
     * @param metaName meta name to extract a numeric meta value, must not be a blank
     * @return comparator
     */
    public static Comparator<Story> byNumericMetaValue(String metaName)
    {
        isTrue(metaName.matches("[^ ]+"), "The meta name must be a non-zero length string without spaces");
        return Comparator.comparing(story -> getNumericMetaValue(story, metaName), Comparator.reverseOrder());
    }

    private static int getNumericMetaValue(Story story, String metaName)
    {
        Meta meta = story.getMeta();
        if (!meta.hasProperty(metaName))
        {
            return 0;
        }
        String metaValue = meta.getProperty(metaName);
        isTrue(NumberUtils.isDigits(metaValue), "The meta value is expected to be a number, but got '%s'", metaValue);
        return Integer.parseInt(metaValue);
    }
}
