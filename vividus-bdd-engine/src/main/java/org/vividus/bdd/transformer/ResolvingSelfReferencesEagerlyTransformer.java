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

package org.vividus.bdd.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterControls;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("RESOLVING_SELF_REFERENCES_EAGERLY")
public class ResolvingSelfReferencesEagerlyTransformer implements ExtendedTableTransformer
{
    private final ParameterControls parameterControls;
    private final Pattern placeholderPattern;

    public ResolvingSelfReferencesEagerlyTransformer(ParameterControls parameterControls)
    {
        this.parameterControls = parameterControls;
        placeholderPattern = Pattern.compile(
                parameterControls.nameDelimiterLeft() + "(.*?)" + parameterControls.nameDelimiterRight());
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        TableRows tableRows = tableParsers.parseRows(tableAsString, properties);
        return ExamplesTableProcessor.buildExamplesTable(tableRows.getHeaders(), resolveRows(tableRows.getRows()),
                properties, true);
    }

    private List<List<String>> resolveRows(List<Map<String, String>> rows)
    {
        return rows.stream().map(this::resolveRow).collect(Collectors.toList());
    }

    private List<String> resolveRow(Map<String, String> unresolvedRow)
    {
        Map<String, String> resolvedRow = new HashMap<>(unresolvedRow.size(), 1);
        return unresolvedRow.keySet().stream()
                .map(name -> resolveCell(name, resolvedRow, unresolvedRow))
                .collect(Collectors.toList());
    }

    private String resolveCell(String name, Map<String, String> resolvedRow, Map<String, String> unresolvedRow)
    {
        return resolveCell(name, new ArrayList<>(), resolvedRow, unresolvedRow);
    }

    private String resolveCell(String name, List<String> resolutionChain, Map<String, String> resolvedRow,
            Map<String, String> unresolvedRow)
    {
        if (resolvedRow.containsKey(name))
        {
            return resolvedRow.get(name);
        }
        resolutionChain.add(name);
        String result = unresolvedRow.get(name);
        Matcher matcher = placeholderPattern.matcher(result);
        while (matcher.find())
        {
            String nestedName = matcher.group(1);
            Validate.validState(!name.equals(nestedName), "Circular self reference is found in column '%s'", name);
            checkForCircularChainOfReferences(resolutionChain, nestedName);
            if (unresolvedRow.containsKey(nestedName))
            {
                resolveCell(nestedName, resolutionChain, resolvedRow, unresolvedRow);
            }
            result = parameterControls.replaceAllDelimitedNames(result, nestedName, resolvedRow.get(nestedName));
        }
        resolvedRow.put(name, result);
        return result;
    }

    private void checkForCircularChainOfReferences(List<String> resolutionChain, String name)
    {
        int index = resolutionChain.indexOf(name);
        if (index >= 0)
        {
            String delimiter = " -> ";
            String truncatedChain = resolutionChain.stream().skip(index).collect(Collectors.joining(delimiter));
            throw new IllegalStateException(
                    "Circular chain of references is found: " + truncatedChain + delimiter + name);
        }
    }
}
