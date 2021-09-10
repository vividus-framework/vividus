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

package org.vividus.bdd.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class DataSourceManagerTests
{
    private static final String DB_KEY = "dbKey";
    private static final String MISSING_DB_CONFIG_ERROR = "Database connection with key '%s' is not configured in "
            + "properties";

    @Mock private PropertyMappedCollection<DriverManagerDataSource> dataSources;
    @InjectMocks private DataSourceManager dataSourceManager;

    @Test
    void shouldReturnDataSourceByKey()
    {
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        when(dataSources.get(DB_KEY, MISSING_DB_CONFIG_ERROR, DB_KEY)).thenReturn(dataSource);
        assertEquals(dataSource, dataSourceManager.getDataSource(DB_KEY));
    }

    @Test
    void shouldReturnSameJdbcTemplate()
    {
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        when(dataSources.get(DB_KEY, MISSING_DB_CONFIG_ERROR, DB_KEY)).thenReturn(dataSource);
        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplate(DB_KEY);
        assertEquals(jdbcTemplate, dataSourceManager.getJdbcTemplate(DB_KEY));
    }
}
