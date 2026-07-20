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

package org.vividus.report.allure.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

public class CustomTranslationsPlugin extends DynamicPlugin
{
    public CustomTranslationsPlugin(PropertyMappedCollection<Map<String, ?>> customTranslations, JsonUtils jsonUtils)
            throws IOException
    {
        super("custom-translations", "index.js", () -> {
            if (customTranslations.getData().isEmpty())
            {
                return List.of();
            }
            List<String> jsFileLines = new ArrayList<>();
            jsFileLines.add("(function() {");
            jsFileLines.add("  var translations = {};");
            customTranslations.getData().forEach((lang, value) -> jsFileLines.add(
                            "  translations['%s'] = %s;".formatted(lang, jsonUtils.toJson(value))
                    )
            );
            jsFileLines.add("  var lang = document.documentElement.lang || 'en';");
            jsFileLines.add("  var t = translations[lang] || translations['en'] || {};");
            jsFileLines.add("  function applyTranslations(obj, prefix) {");
            jsFileLines.add("    Object.keys(obj).forEach(function(key) {");
            jsFileLines.add("      var fullKey = prefix ? prefix + '.' + key : key;");
            jsFileLines.add("      if (typeof obj[key] === 'object' && obj[key] !== null) {");
            jsFileLines.add("        applyTranslations(obj[key], fullKey);");
            jsFileLines.add("      }");
            jsFileLines.add("    });");
            jsFileLines.add("  }");
            jsFileLines.add("  if (t) { applyTranslations(t, ''); }");
            jsFileLines.add("  window.__vividusCustomTranslations = translations;");
            jsFileLines.add("})();");
            return jsFileLines;
        });
    }
}
