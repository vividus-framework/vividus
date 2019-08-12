/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.transformer;

import javax.inject.Inject;
import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableProperties;
import org.vividus.bdd.spring.Configuration;

@Named("JOINING")
public class JoiningTableTransformer implements ExtendedTableTransformer
{
    @Inject private Configuration configuration;

    @Override
    public String transform(String tableAsString, ExamplesTableProperties properties)
    {
        JoinMode joinMode = getMandatoryEnumProperty(properties, "joinMode", JoinMode.class);
        ExamplesTable examplesTable = configuration.getExamplesTableFactory().createExamplesTable(tableAsString);
        return joinMode.join(examplesTable, properties);
    }
}
