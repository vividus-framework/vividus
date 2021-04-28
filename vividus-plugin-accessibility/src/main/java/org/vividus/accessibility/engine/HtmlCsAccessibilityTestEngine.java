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

package org.vividus.accessibility.engine;

import java.util.List;

import javax.inject.Named;

import org.vividus.accessibility.model.AccessibilityCheckOptions;
import org.vividus.accessibility.model.AccessibilityViolation;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.json.ObjectMapperFactory;

@Named
public class HtmlCsAccessibilityTestEngine implements AccessibilityTestEngine
{
    private static final String HTML_CS_JS =
        ResourceUtils.loadResource(HtmlCsAccessibilityTestEngine.class, "HTMLCS.js");
    private static final String PA11Y_JS =
        ResourceUtils.loadResource(HtmlCsAccessibilityTestEngine.class, "pa11y.js");

    private final WebJavascriptActions webJavascriptActions;
    private final JsonUtils jsonUtils = new JsonUtils(ObjectMapperFactory.createWithCaseInsensitiveEnumDeserializer());

    public HtmlCsAccessibilityTestEngine(WebJavascriptActions webJavascriptActions)
    {
        this.webJavascriptActions = webJavascriptActions;
    }

    @Override
    public List<AccessibilityViolation> analyze(AccessibilityCheckOptions options)
    {
        String optionsJson = jsonUtils.toJson(options);
        String script = HTML_CS_JS + PA11Y_JS + "injectPa11y(window, " + optionsJson
                + ", arguments[arguments.length - 1]);";
        String result = webJavascriptActions.executeAsyncScript(script);
        return jsonUtils.toObjectList(result, AccessibilityViolation.class);
    }
}
