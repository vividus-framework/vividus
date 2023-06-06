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

package org.vividus.accessibility.executor;

import java.util.List;

import org.vividus.accessibility.model.AbstractAccessibilityCheckOptions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.json.ObjectMapperFactory;

public class AccessibilityTestExecutor
{
    private final JsonUtils jsonUtils = new JsonUtils(ObjectMapperFactory.createWithCaseInsensitiveEnumDeserializer());

    private final WebJavascriptActions webJavascriptActions;

    public AccessibilityTestExecutor(WebJavascriptActions webJavascriptActions)
    {
        this.webJavascriptActions = webJavascriptActions;
    }

    public <T> List<T> execute(AccessibilityEngine engine, AbstractAccessibilityCheckOptions options,
            Class<T> outputType)
    {
        String optionsJson = jsonUtils.toJson(options);
        String script = engine.getScript() + engine.getRunner() + "injectAccessibilityCheck(window, " + optionsJson
                + ", arguments[0], arguments[1], arguments[2], arguments[arguments.length - 1]);";
        String result = webJavascriptActions.executeAsyncScript(script, options.getRootElement().orElse(null),
                options.getElementsToCheck(), options.getHideElements());
        return jsonUtils.toObjectList(result, outputType);
    }
}
