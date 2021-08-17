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

package org.vividus.azure.devops.facade.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public final class Step
{
    @JacksonXmlProperty(isAttribute = true)
    private final String id;
    @JacksonXmlProperty(isAttribute = true)
    private final String type;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "parameterizedString")
    private final List<ParameterizedString> parameterizedStrings;
    private final Object description;

    public Step(int id, String actionText, String resultText)
    {
        this.id = String.valueOf(id);
        this.type = "ActionStep";
        this.parameterizedStrings = List.of(
            new ParameterizedString(actionText),
            new ParameterizedString(resultText)
        );
        this.description = new Object();
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public List<ParameterizedString> getParameterizedStrings()
    {
        return parameterizedStrings;
    }

    public Object getDescription()
    {
        return description;
    }
}
