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

package org.vividus.bdd;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.vividus.bdd.resource.ResourceBatch;
import org.vividus.util.property.IPropertyMapper;

public class BatchStorage implements IBatchStorage
{
    private static final String BATCH = "batch-";
    private static final Pattern FORMAT_PATTERN = Pattern.compile(BATCH + "\\d+");

    private final Map<String, ResourceBatch> batches;

    public BatchStorage(IPropertyMapper propertyMapper, String propertyPrefix) throws IOException
    {
        batches = propertyMapper.readValues(propertyPrefix, ResourceBatch.class).entrySet().stream()
                .filter(e -> FORMAT_PATTERN.matcher(e.getKey()).matches())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> null,
                    () -> new TreeMap<>(Comparator.comparingInt(BatchStorage::getBatchNumber))));
    }

    private static int getBatchNumber(String batchName)
    {
        return Integer.parseInt(StringUtils.removeStart(batchName, BATCH));
    }

    @Override
    public ResourceBatch getBatch(String batchKey)
    {
        return getBatches().get(batchKey);
    }

    @Override
    public Map<String, ResourceBatch> getBatches()
    {
        return batches;
    }
}
