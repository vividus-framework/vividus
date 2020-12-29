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

package org.vividus.bdd.converter;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;

@Named
public class StringToBsonConverter extends FunctionalParameterConverter<Bson>
{
    public StringToBsonConverter()
    {
        super(value -> Document.parse(StringUtils.defaultIfBlank(value, "{}")));
    }
}
