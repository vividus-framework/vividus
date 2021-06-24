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

package org.vividus.bdd.converter;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import com.browserup.harreader.model.HttpMethod;

import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;

@Named
public class HttpMethodSetConverter extends FunctionalParameterConverter<String, Set<HttpMethod>>
{
    public HttpMethodSetConverter()
    {
        super(value -> Stream.of(value.split("(or|,)"))
                             .map(String::trim)
                             .map(HttpMethod::valueOf)
                             .collect(Collectors.toCollection(() -> EnumSet.noneOf(HttpMethod.class)))
        );
    }
}
