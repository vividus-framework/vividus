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

package org.vividus.excel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.vividus.util.ResourceUtils;

public class ExcelSheetsExtractor implements IExcelSheetsExtractor
{
    private final List<Sheet> sheets;

    public ExcelSheetsExtractor(byte[] bytes) throws WorkbookParsingException
    {
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes)))
        {
            sheets = getAllSheetsFromWorkbook(wb);
        }
        catch (EncryptedDocumentException | IOException e)
        {
            throw new WorkbookParsingException(e);
        }
    }

    public ExcelSheetsExtractor(String path) throws WorkbookParsingException
    {
        try (Workbook wb = WorkbookFactory
                .create(ResourceUtils.loadFile(this.getClass(), StringUtils.prependIfMissing(path, "/"))))
        {
            sheets = getAllSheetsFromWorkbook(wb);
        }
        catch (EncryptedDocumentException | IOException e)
        {
            throw new WorkbookParsingException(e);
        }
    }

    @Override
    public Optional<Sheet> getSheet(String name)
    {
        return sheets.stream().filter(s -> name.equals(s.getSheetName())).findFirst();
    }

    @Override
    public Optional<Sheet> getSheet(int index)
    {
        return sheets.size() < index ? Optional.empty() : Optional.of(sheets.get(index));
    }

    @Override
    public Map<String, Sheet> getSheetsWithNames()
    {
        return sheets.stream().collect(
                Collectors.toMap(Sheet::getSheetName, Function.identity(), (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public List<Sheet> getSheets()
    {
        return sheets;
    }

    private List<Sheet> getAllSheetsFromWorkbook(Workbook wb)
    {
        return IntStream.range(0, wb.getNumberOfSheets()).mapToObj(wb::getSheetAt)
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
