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

package org.vividus.bdd.converter.ui.web;

import java.lang.reflect.Type;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractChainableParameterConverter;
import org.jbehave.core.steps.Parameters;

import io.netty.handler.codec.http.DefaultHttpHeaders;

@Named
public class ExamplesTableToDefaultHttpHeadersConverter extends AbstractChainableParameterConverter<ExamplesTable,
    DefaultHttpHeaders>
{
    @Override
    public DefaultHttpHeaders convertValue(ExamplesTable value, Type type)
    {
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        value.getRowsAsParameters(true)
             .forEach(row -> httpHeaders.add(getRowValue("name", row), getRowValue("value", row)));
        return httpHeaders;
    }

    private String getRowValue(String name, Parameters row)
    {
        return row.valueAs(name, String.class);
    }
}
