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

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

public final class QueryStatistic
{
    private final StopWatch stopwatch = new StopWatch();
    private final String url;
    private long rowsQuantity;
    private String query;
    private Long noPair;

    QueryStatistic(String url)
    {
        this.url = url;
    }

    public void start()
    {
        stopwatch.start();
    }

    public void end()
    {
        stopwatch.stop();
    }

    public String getExecutionTime()
    {
        return DurationFormatUtils.formatDurationHMS(stopwatch.getTime());
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public long getRowsQuantity()
    {
        return rowsQuantity;
    }

    public void setRowsQuantity(long rowsQuantity)
    {
        this.rowsQuantity = rowsQuantity;
    }

    public Long getNoPair()
    {
        return noPair;
    }

    public void setNoPair(Long noPair)
    {
        this.noPair = noPair;
    }

    public String getUrl()
    {
        return url;
    }
}
