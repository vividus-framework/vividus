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

import java.util.List;
import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;

public class MergingTableTransformer extends AbstractTableLoadingTransformer
{
    public MergingTableTransformer()
    {
        super(false);
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        MergeMode mergeMode = properties.getMandatoryNonBlankProperty("mergeMode", MergeMode.class);

        List<ExamplesTable> examplesTables = loadTables(tableAsString, properties);
        String fillerValue = properties.getProperties().getProperty("fillerValue");
        return mergeMode.merge(getConfiguration().keywords(), getConfiguration().parameterConverters(), examplesTables,
                properties, Optional.ofNullable(fillerValue));
    }
}
