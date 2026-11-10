/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.csv.transformer;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.vividus.util.ResourceUtils.loadResourceOrFileAsStream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.csv.CsvReader;
import org.vividus.transformer.ExtendedTableTransformer;
import org.vividus.util.ExamplesTableProcessor;

public class CsvTableTransformer implements ExtendedTableTransformer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvTableTransformer.class);
    private static final String VARIABLE_NAME_PROPERTY = "variableName";
    private static final String PATH_PROPERTY = "path";
    private static final String DEPRECATED_CSV_PATH_PROPERTY = "csvPath";

    private final CSVFormat defaultCsvFormat;
    private final VariableContext variableContext;

    public CsvTableTransformer(CSVFormat csvFormat, VariableContext variableContext)
    {
        this.defaultCsvFormat = csvFormat;
        this.variableContext = variableContext;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties tableProperties)
    {
        checkTableEmptiness(tableAsString);
        Properties properties = tableProperties.getProperties();

        Map.Entry<String, String> entry = processCompetingMandatoryProperties(properties,
                VARIABLE_NAME_PROPERTY, PATH_PROPERTY, DEPRECATED_CSV_PATH_PROPERTY);
        String sourceKey = entry.getKey();
        String sourceValue = entry.getValue();
        checkDeprecatedProperty(sourceKey);

        CSVFormat csvFormat = defaultCsvFormat;
        String delimiter = properties.getProperty("delimiterChar");
        if (delimiter != null)
        {
            int delimiterLength = delimiter.length();
            isTrue(delimiterLength == 1, "CSV delimiter must be a single char, but value '%s' has length of %d",
                    delimiter, delimiterLength);
            csvFormat = csvFormat.builder().setDelimiter(delimiter.charAt(0)).get();
        }

        try
        {
            List<Map<String, String>> result = VARIABLE_NAME_PROPERTY.equals(sourceKey)
                    ? readCsvFromVariable(csvFormat, sourceValue)
                    : readCsvFromFile(csvFormat, sourceValue);
            return ExamplesTableProcessor.buildExamplesTable(result.get(0).keySet(), extractValues(result),
                    tableProperties, true);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Problem during CSV data reading", e);
        }
    }

    private List<Map<String, String>> readCsvFromFile(CSVFormat csvFormat, String path) throws IOException
    {
        return new CsvReader(csvFormat).readCsvStream(loadResourceOrFileAsStream(path));
    }

    private List<Map<String, String>> readCsvFromVariable(CSVFormat csvFormat, String variableName) throws IOException
    {
        String variableValue = variableContext.getVariable(variableName);
        isTrue(StringUtils.isNotEmpty(variableValue), "Variable '%s' is not set or empty."
                + " Please check that variable is defined and has 'global' or 'next_batches' scope", variableName);
        List<Map<String, String>> result = new CsvReader(csvFormat).readCsvString(variableValue);
        notEmpty(result, "Unable to create examples table based on '%s' variable value."
                + " Please check that value has proper csv format", variableName);
        return result;
    }

    /**
     * @param propertyKey actual property key from table transformer parameters
     * @deprecated This method will be removed in VIVIDUS 0.7.0
     */
    @Deprecated(since = "0.6.13", forRemoval = true)
    private void checkDeprecatedProperty(String propertyKey)
    {
        if (DEPRECATED_CSV_PATH_PROPERTY.equals(propertyKey))
        {
            LOGGER.atWarn().log("The 'csvPath' transformer parameter is deprecated and will be removed VIVIDUS 0.7.0, "
                    + "please use 'path' parameter instead.");
        }
    }

    private List<List<String>> extractValues(List<Map<String, String>> data)
    {
        return data.stream()
                .map(Map::values)
                .map(ArrayList::new)
                .collect(toList());
    }
}
