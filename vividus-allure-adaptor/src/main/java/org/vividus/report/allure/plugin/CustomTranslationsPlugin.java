/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.report.allure.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class CustomTranslationsPlugin extends DynamicPlugin
{
    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public CustomTranslationsPlugin(PropertyMappedCollection<Map<String, ?>> customTranslations, JsonUtils jsonUtils)
            throws IOException
    {
        super("custom-translations", () -> {
            List<String> jsFileLines = new ArrayList<>();
            customTranslations.getData().forEach((lang, value) -> jsFileLines.add(
                            "allure.api.addTranslation('%s', %s);".formatted(lang, jsonUtils.toJson(value))
                    )
            );
            return jsFileLines;
        });
    }
}
