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

package org.vividus.jackson.databind;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Named;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

@Named
public class PatternDeserializer extends JsonDeserializer<Pattern>
{
    @Override
    public Pattern deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        return Optional.ofNullable(p.getText())
                       .map(Pattern::compile)
                       .orElseThrow(() -> new IllegalArgumentException("Pattern could not be empty"));
    }
}
