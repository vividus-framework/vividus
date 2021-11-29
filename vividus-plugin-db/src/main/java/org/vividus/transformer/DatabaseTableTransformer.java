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

package org.vividus.transformer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.springframework.dao.EmptyResultDataAccessException;
import org.vividus.db.DataSourceManager;
import org.vividus.util.ExamplesTableProcessor;

@Named("FROM_DB")
public class DatabaseTableTransformer implements ExtendedTableTransformer
{
    private final DataSourceManager dataSourceManager;

    public DatabaseTableTransformer(DataSourceManager dataSourceManager)
    {
        this.dataSourceManager = dataSourceManager;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        String dbKey = properties.getMandatoryNonBlankProperty("dbKey", String.class);
        String sqlQuery = properties.getMandatoryNonBlankProperty("sqlQuery", String.class);
        String nullReplacement = properties.getProperties().getProperty("nullReplacement");

        List<Map<String, Object>> result = dataSourceManager.getJdbcTemplate(dbKey).queryForList(sqlQuery);
        if (result.isEmpty())
        {
            throw new EmptyResultDataAccessException("Result was expected to have at least one row", 1);
        }
        return ExamplesTableProcessor.buildExamplesTable(
                result.get(0).keySet(),
                result.stream()
                        .map(map -> map
                                .entrySet()
                                .stream()
                                .collect(LinkedHashMap<String, String>::new,
                                        (m, v) -> m.put(
                                                v.getKey(),
                                                Optional.ofNullable(v.getValue()).map(Object::toString)
                                                        .orElse(nullReplacement)),
                                        LinkedHashMap::putAll
                                ).values()
                        ).map(ArrayList::new).collect(toList()),
                properties, true);
    }
}
