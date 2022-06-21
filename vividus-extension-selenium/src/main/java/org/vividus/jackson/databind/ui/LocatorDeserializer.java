/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.jackson.databind.ui;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.vividus.ui.action.search.Locator;
import org.vividus.ui.util.LocatorConversionUtils;

@Named
public class LocatorDeserializer extends JsonDeserializer<Locator>
{
    @Inject private LocatorConversionUtils conversionUtils;

    @Override
    public Locator deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        return conversionUtils.convertToLocator(p.getText());
    }
}
