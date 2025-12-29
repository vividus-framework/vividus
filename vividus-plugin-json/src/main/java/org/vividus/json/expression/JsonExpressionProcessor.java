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

package org.vividus.json.expression;

import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.vividus.util.json.JsonJackson3Utils;

/**
 * Formats any JSON into a one line.
 */
public class JsonExpressionProcessor extends SingleArgExpressionProcessor<String>
{
    public JsonExpressionProcessor(JsonJackson3Utils jsonUtils)
    {
        super("formatToOneLineJson", json -> jsonUtils.readTree(json).toString());
    }
}
