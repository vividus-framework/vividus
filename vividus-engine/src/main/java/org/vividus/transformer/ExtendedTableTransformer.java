/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.transformer;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableTransformers.TableTransformer;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ExtendedTableTransformer extends TableTransformer
{
    default void checkTableEmptiness(String tableAsString)
    {
        isTrue(StringUtils.isBlank(tableAsString), "Input table must be empty");
    }

    @Deprecated(since = "0.6.13", forRemoval = true)
    default <T extends Entry<String, Function<String, R>>, R> R processCompetingMandatoryProperties(
            TableProperties tableProperties, T processor1, T processor2)
    {
        String propertyName1 = processor1.getKey();
        String propertyName2 = processor2.getKey();
        String propertyValue1 = tableProperties.getProperties().getProperty(propertyName1);
        String propertyValue2 = tableProperties.getProperties().getProperty(propertyName2);
        if (propertyValue1 != null)
        {
            isTrue(propertyValue2 == null,
                    "Only one ExamplesTable property must be set, but found both '%s' and '%s'", propertyName1,
                    propertyName2);
            return processor1.getValue().apply(propertyValue1);
        }
        else
        {
            isTrue(propertyValue2 != null, "One of ExamplesTable properties must be set: either '%s' or '%s'",
                    propertyName1, propertyName2);
            return processor2.getValue().apply(propertyValue2);
        }
    }

    /**
     * Validates that only one property actually defined in the transformer and checks that its value is not empty
     * @param properties TableTransformer properties
     * @param propertyKeys Array of competing mandatory property keys
     * @return Entry with property key and value
     */
    default Entry<String, String> processCompetingMandatoryProperties(Properties properties, String... propertyKeys)
    {
        Map<String, String> actualProperties = new HashMap<>();
        List.of(propertyKeys).forEach(v -> Optional.ofNullable(properties.getProperty(v))
                .ifPresent(o -> actualProperties.put(v, o)));

        String propertyKeysForErrorMsg = String.join("', '", Arrays.copyOf(propertyKeys, propertyKeys.length - 1));
        isTrue(actualProperties.size() == 1, "One of either '" + propertyKeysForErrorMsg + "' or '"
                + propertyKeys[propertyKeys.length - 1] + "' should be specified");

        Map.Entry<String, String> mandatoryProperty = actualProperties.entrySet().iterator().next();
        String mandatoryPropertyKey = mandatoryProperty.getKey();
        String mandatoryPropertyValue = mandatoryProperty.getValue();
        notBlank(mandatoryPropertyValue, String.format("ExamplesTable property '%s' is blank", mandatoryPropertyKey));

        return mandatoryProperty;
    }
}
