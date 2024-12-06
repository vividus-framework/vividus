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

package org.vividus.util.databind;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.commons.lang3.StringUtils;
import org.vividus.util.UriUtils;

public class CommonJacksonMapperModule extends SimpleModule
{
    private static final long serialVersionUID = -7139230038016853680L;

    public CommonJacksonMapperModule()
    {
        addDeserializer(Pattern.class, new JsonDeserializer<>()
        {
            @Override
            public Pattern deserialize(JsonParser parser, DeserializationContext context) throws IOException
            {
                return Pattern.compile(parser.getText());
            }
        });

        addDeserializer(URI.class, new JsonDeserializer<>()
        {
            @Override
            public URI deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException
            {
                return Optional.of(parser.getText()).filter(StringUtils::isNoneEmpty).map(UriUtils::createUri)
                        .orElse(null);
            }
        });
    }
}
