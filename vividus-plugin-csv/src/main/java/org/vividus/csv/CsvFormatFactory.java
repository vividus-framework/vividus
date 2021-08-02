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

package org.vividus.csv;

import org.apache.commons.csv.CSVFormat;

public class CsvFormatFactory
{
    private final CSVFormat csvFormat;

    public CsvFormatFactory(char delimiter, Character escape)
    {
        this.csvFormat = CSVFormat.DEFAULT.builder().setDelimiter(delimiter).setEscape(escape).build();
    }

    public CSVFormat getCsvFormat()
    {
        return csvFormat;
    }
}
