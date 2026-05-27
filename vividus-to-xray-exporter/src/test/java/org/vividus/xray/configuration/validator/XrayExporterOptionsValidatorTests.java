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

package org.vividus.xray.configuration.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.UncheckedIOException;
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
import org.vividus.xray.configuration.XrayExporterOptions.CloudOptions;
import org.vividus.xray.configuration.XrayExporterOptions.TestExecutionOptions;

@ExtendWith(MockitoExtension.class)
class XrayExporterOptionsValidatorTests
{
    private static final String TEST_EXECUTION_ATTACHMENTS_FIELD = "test-execution.attachments";
    private static final String CLOUD_CLIENT_ID_FIELD = "cloud.client-id";
    private static final String CLOUD_CLIENT_SECRET_FIELD = "cloud.client-secret";
    private static final String DATA_TXT = "data.txt";
    private static final String CLIENT_ID = "id";
    private static final String CLIENT_SECRET = "secret";

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
        XrayExporterOptions options = createOptions(List.of(directory.getRoot()));

        validator.validate(options, errors);

        verify(errors).rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                "Please do not try to publish the file system root as the attachment");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldRejectIfAttachmentPathPointsToNonExistentFile()
    {
        Path nonExstentPath = Paths.get("path/to/nowhere");
        XrayExporterOptions options = createOptions(List.of(nonExstentPath));

        validator.validate(options, errors);

        verify(errors).rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                "The attachment file at path " + nonExstentPath + " does not exist");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldRejectIfAttachmentDirectoryIsEmpty(@TempDir Path directory)
    {
        XrayExporterOptions options = createOptions(List.of(directory));

        validator.validate(options, errors);

        verify(errors).rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                "The attachment folder at path " + directory + " is empty");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldThrowUncheckedIoExceptionWhenDirectoryListingFails(@TempDir Path directory)
    {
        assumeTrue(directory.toFile().setReadable(false),
                "Cannot remove directory read permissions (likely running as root)");
        try
        {
            XrayExporterOptions options = createOptions(List.of(directory));
            assertThrows(UncheckedIOException.class, () -> validator.validate(options, errors));
        }
        finally
        {
            directory.toFile().setReadable(true);
        }
    }

    @Test
    void shouldPassValidationIfFolderExists(@TempDir Path directory) throws IOException
    {
        Files.createFile(directory.resolve(DATA_TXT));
        XrayExporterOptions options = createOptions(List.of(directory));

        validator.validate(options, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void shouldPassValidationIfRegularFileExists(@TempDir Path directory) throws IOException
    {
        Path filePath = Files.createFile(directory.resolve(DATA_TXT));
        XrayExporterOptions options = createOptions(List.of(filePath));

        validator.validate(options, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void shouldRejectCloudClientIdIfCloudEnabledButClientIdIsBlank()
    {
        XrayExporterOptions options = createOptions(List.of());
        CloudOptions cloud = new CloudOptions();
        cloud.setEnabled(true);
        cloud.setClientSecret(CLIENT_SECRET);
        options.setCloudOptions(cloud);

        validator.validate(options, errors);

        verify(errors).rejectValue(CLOUD_CLIENT_ID_FIELD, StringUtils.EMPTY,
                "Xray Cloud client ID must be set when cloud mode is enabled");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldRejectCloudClientSecretIfCloudEnabledButClientSecretIsBlank()
    {
        XrayExporterOptions options = createOptions(List.of());
        CloudOptions cloud = new CloudOptions();
        cloud.setEnabled(true);
        cloud.setClientId(CLIENT_ID);
        options.setCloudOptions(cloud);

        validator.validate(options, errors);

        verify(errors).rejectValue(CLOUD_CLIENT_SECRET_FIELD, StringUtils.EMPTY,
                "Xray Cloud client secret must be set when cloud mode is enabled");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldRejectCloudClientIdIfCloudDisabledButClientIdIsSet()
    {
        XrayExporterOptions options = createOptions(List.of());
        CloudOptions cloud = new CloudOptions();
        cloud.setClientId(CLIENT_ID);
        options.setCloudOptions(cloud);

        validator.validate(options, errors);

        verify(errors).rejectValue(CLOUD_CLIENT_ID_FIELD, StringUtils.EMPTY,
                "Xray Cloud client ID must not be set when cloud mode is disabled");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldRejectCloudClientSecretIfCloudDisabledButClientSecretIsSet()
    {
        XrayExporterOptions options = createOptions(List.of());
        CloudOptions cloud = new CloudOptions();
        cloud.setClientSecret(CLIENT_SECRET);
        options.setCloudOptions(cloud);

        validator.validate(options, errors);

        verify(errors).rejectValue(CLOUD_CLIENT_SECRET_FIELD, StringUtils.EMPTY,
                "Xray Cloud client secret must not be set when cloud mode is disabled");
        verifyNoMoreInteractions(errors);
    }

    @Test
    void shouldPassValidationWhenCloudEnabledWithCredentials()
    {
        XrayExporterOptions options = createOptions(List.of());
        CloudOptions cloud = new CloudOptions();
        cloud.setEnabled(true);
        cloud.setClientId(CLIENT_ID);
        cloud.setClientSecret(CLIENT_SECRET);
        options.setCloudOptions(cloud);

        validator.validate(options, errors);

        verifyNoInteractions(errors);
    }

    private XrayExporterOptions createOptions(List<Path> attachments)
    {
        XrayExporterOptions options = new XrayExporterOptions();
        TestExecutionOptions executionOptions = new TestExecutionOptions();
        executionOptions.setAttachments(attachments);
        options.setTestExecutionOptions(executionOptions);
        return options;
    }
}
