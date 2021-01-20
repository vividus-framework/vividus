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

package org.vividus.bdd.steps;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ExcelFileStepsTests
{
    @Mock private IBddVariableContext bddVariableContext;

    @InjectMocks private ExcelFileSteps fileSteps;

    @Test
    void testInitVariableUsingExcelFilePath() throws IOException
    {
        ExamplesTable content = new ExamplesTable("|k1|\n|v1|");
        String pathVariable = "path";
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        fileSteps.initVariableUsingExcelFilePath(content, scopes, pathVariable);
        verify(bddVariableContext).putVariable(eq(scopes), eq(pathVariable), argThat(path ->
        {
            try (XSSFWorkbook myExcelBook = new XSSFWorkbook(FileUtils.openInputStream(new File(path.toString()))))
            {
                XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);
                String header = myExcelSheet.getRow(0).getCell(0).getStringCellValue();
                String values = header + myExcelSheet.getRow(1).getCell(0).getStringCellValue();
                return "k1v1".equals(values);
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }));
    }
}
