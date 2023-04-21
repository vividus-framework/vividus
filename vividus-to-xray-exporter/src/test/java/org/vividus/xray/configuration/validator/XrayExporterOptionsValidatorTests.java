/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.xray.configuration.validator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.vividus.xray.configuration.XrayExporterOptions;

@ExtendWith(MockitoExtension.class)
class XrayExporterOptionsValidatorTests
{
    private static final String TEST_EXECUTION_ATTACHMENTS_FIELD = "test-execution-attachments";
    private static final String DATA_TXT = "data.txt";

    @Mock private Errors errors;

    private final XrayExporterOptionsValidator validator = new XrayExporterOptionsValidator();

    @Test
    void shouldSupportXrayExporterOptionsClass()
    {
        assertTrue(validator.supports(XrayExporterOptions.class));
    }

    @Test
    void shouldRejectIfAttachmentPathPointsToTheRoot(@TempDir Path directory)
    {
        XrayExporterOptions options = new XrayExporterOptions();
        options.setTestExecutionAttachments(List.of(directory.getRoot()));

        validator.validate(options, errors);

        verify(errors).rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                "Please do not try to publish the file system root as the attachment");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldRejectIfAttachmentPathPointsToNonExistentFile()
    {
        XrayExporterOptions options = new XrayExporterOptions();
        Path nonExstentPath = Paths.get("path/to/nowhere");
        options.setTestExecutionAttachments(List.of(nonExstentPath));

        validator.validate(options, errors);

        verify(errors).rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                "The attachment file at path " + nonExstentPath + " does not exist");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldRejectIfAttachmentDirectoryIsEmpty(@TempDir Path directory)
    {
        XrayExporterOptions options = new XrayExporterOptions();
        options.setTestExecutionAttachments(List.of(directory));

        validator.validate(options, errors);

        verify(errors).rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                "The attachment folder at path " + directory + " is empty");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldPassValidationIfFolderExists(@TempDir Path directory) throws IOException
    {
        Files.createFile(directory.resolve(DATA_TXT));
        XrayExporterOptions options = new XrayExporterOptions();
        options.setTestExecutionAttachments(List.of(directory));

        validator.validate(options, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void shouldPassValidationIfRegularFileExists(@TempDir Path directory) throws IOException
    {
        Path filePath = Files.createFile(directory.resolve(DATA_TXT));
        XrayExporterOptions options = new XrayExporterOptions();
        options.setTestExecutionAttachments(List.of(filePath));

        validator.validate(options, errors);

        verifyNoInteractions(errors);
    }
}
