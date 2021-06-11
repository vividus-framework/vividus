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

package org.vividus.bdd.steps.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public final class DataSourceStatistics
{
    private long totalRows;
    private long mismatched;
    private final QueryStatistic left;
    private final QueryStatistic right;

    DataSourceStatistics(JdbcTemplate leftJdbcTemplate)
    {
        left = createQueryStatistic(leftJdbcTemplate);
        right = new QueryStatistic(null);
    }

    DataSourceStatistics(JdbcTemplate leftJdbcTemplate, JdbcTemplate rightJdbcTemplate)
    {
        left = createQueryStatistic(leftJdbcTemplate);
        right = createQueryStatistic(rightJdbcTemplate);
    }

    private QueryStatistic createQueryStatistic(JdbcTemplate jdbcTemplate)
    {
        String url = ((DriverManagerDataSource) jdbcTemplate.getDataSource()).getUrl();
        return new QueryStatistic(url);
    }

    public long getMismatched()
    {
        return mismatched;
    }

    public long getMatched()
    {
        return totalRows - mismatched;
    }

    public void setMismatched(long mismatched)
    {
        this.mismatched = mismatched;
    }

    public long getTotalRows()
    {
        return totalRows;
    }

    public void setTotalRows(long totalRows)
    {
        this.totalRows = totalRows;
    }

    public QueryStatistic getLeft()
    {
        return left;
    }

    public QueryStatistic getRight()
    {
        return right;
    }
}
