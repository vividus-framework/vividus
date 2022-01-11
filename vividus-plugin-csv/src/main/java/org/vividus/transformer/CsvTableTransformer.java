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
import static org.apache.commons.lang3.Validate.isTrue;
import static org.vividus.util.ResourceUtils.findResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.csv.CsvReader;
import org.vividus.util.ExamplesTableProcessor;

@Named("FROM_CSV")
public class CsvTableTransformer implements ExtendedTableTransformer
{
    private final CSVFormat defaultCsvFormat;

    public CsvTableTransformer(CSVFormat csvFormat)
    {
        this.defaultCsvFormat = csvFormat;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        String csvPath = properties.getMandatoryNonBlankProperty("csvPath", String.class);

        CSVFormat csvFormat = defaultCsvFormat;
        String delimiter = properties.getProperties().getProperty("delimiterChar");
        if (delimiter != null)
        {
            int delimiterLength = delimiter.length();
            isTrue(delimiterLength == 1, "CSV delimiter must be a single char, but value '%s' has length of %d",
                    delimiter, delimiterLength);
            csvFormat = csvFormat.builder().setDelimiter(delimiter.charAt(0)).build();
        }
        try
        {
            List<CSVRecord> result = new CsvReader(csvFormat).readCsvFile(findResource(getClass(), csvPath));
            return ExamplesTableProcessor.buildExamplesTable(result.get(0).toMap().keySet(), extractValues(result),
                    properties, true);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Problem during CSV file reading", e);
        }
    }

    private List<List<String>> extractValues(List<CSVRecord> data)
    {
        return data.stream()
                .map(CSVRecord::toMap)
                .map(Map::values)
                .map(ArrayList::new)
                .collect(toList());
    }
}
