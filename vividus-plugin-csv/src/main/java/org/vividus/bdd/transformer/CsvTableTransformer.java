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

package org.vividus.bdd.transformer;

import static java.util.stream.Collectors.toList;
import static org.vividus.util.ResourceUtils.findResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.csv.CSVRecord;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.util.ExamplesTableProcessor;
import org.vividus.csv.CsvReader;

@Named("FROM_CSV")
public class CsvTableTransformer implements ExtendedTableTransformer
{
    private final CsvReader csvReader;

    public CsvTableTransformer(CsvReader csvReader)
    {
        this.csvReader = csvReader;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        String csvPath = properties.getMandatoryNonBlankProperty("csvPath");
        try
        {
            List<CSVRecord> result = csvReader.readCsvFile(findResource(getClass(), csvPath));
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
                .map(record -> new ArrayList<>(record.toMap().values()))
                .collect(toList());
    }
}
