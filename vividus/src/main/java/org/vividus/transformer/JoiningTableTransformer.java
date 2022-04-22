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

package org.vividus.transformer;

import javax.inject.Inject;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;

public class JoiningTableTransformer implements ExtendedTableTransformer
{
    @Inject private Configuration configuration;

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        JoinMode joinMode = properties.getMandatoryNonBlankProperty("joinMode", JoinMode.class);
        ExamplesTable examplesTable = configuration.examplesTableFactory().createExamplesTable(tableAsString);
        return joinMode.join(examplesTable, properties);
    }
}
