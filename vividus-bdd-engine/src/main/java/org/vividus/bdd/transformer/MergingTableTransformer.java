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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableProperties;
import org.vividus.bdd.spring.Configuration;

@Named("MERGING")
public class MergingTableTransformer implements ExtendedTableTransformer
{
    @Inject private Configuration configuration;

    @Override
    public String transform(String tableAsString, ExamplesTableProperties properties)
    {
        MergeMode mergeMode = getMandatoryEnumProperty(properties, "mergeMode", MergeMode.class);

        List<String> tables = Arrays.stream(properties.getProperties().getProperty("tables").split("(?<!\\\\);"))
                .map(t -> t.replaceAll("\\\\;", ";").trim())
                .distinct()
                .collect(Collectors.toList());
        checkArgument(tables.size() > 1, "Please, specify more than one unique table paths");

        List<ExamplesTable> examplesTables = tables.stream()
                .map(p -> configuration.examplesTableFactory().createExamplesTable(p))
                .collect(Collectors.toCollection(LinkedList::new));
        String fillerValue = properties.getProperties().getProperty("fillerValue");
        return mergeMode.merge(examplesTables, properties, Optional.ofNullable(fillerValue));
    }
}
