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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.vividus.xray.configuration.XrayExporterOptions;

@Component(EnableConfigurationProperties.VALIDATOR_BEAN_NAME)
public class XrayExporterOptionsValidator implements Validator
{
    private static final String TEST_EXECUTION_ATTACHMENTS_FIELD = "test-execution-attachments";

    @Override
    public boolean supports(Class<?> clazz)
    {
        return XrayExporterOptions.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors)
    {
        XrayExporterOptions options = (XrayExporterOptions) target;
        List<Path> attachments = options.getTestExecutionAttachments();

        attachments.forEach(attachment ->
        {
            if (!Files.exists(attachment))
            {
                errors.rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                        "The attachment file at path " + attachment + " does not exist");
                return;
            }

            try
            {
                if (Files.isDirectory(attachment) && Files.list(attachment).findAny().isEmpty())
                {
                    errors.rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                            "The attachment folder at path " + attachment + " is empty");
                    return;
                }
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }

            if (attachment.getRoot().equals(attachment))
            {
                errors.rejectValue(TEST_EXECUTION_ATTACHMENTS_FIELD, StringUtils.EMPTY,
                        "Please do not try to publish the file system root as the attachment");
            }
        });
    }
}
