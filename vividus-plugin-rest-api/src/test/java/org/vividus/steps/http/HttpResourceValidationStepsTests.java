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

package org.vividus.steps.http;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.validation.ResourceValidator;
import org.vividus.http.validation.model.CheckStatus;
import org.vividus.http.validation.model.ResourceValidation;
import org.vividus.reporter.event.AttachmentPublisher;

@ExtendWith(MockitoExtension.class)
class HttpResourceValidationStepsTests
{
    @Mock private ResourceValidator<ResourceValidation> resourceValidator;
    @Mock private AttachmentPublisher attachmentPublisher;
    @InjectMocks private HttpResourceValidationSteps steps;

    @Test
    void shouldVerifyHttpResources()
    {
        String passedUrl = "https://docs.vividus.dev";
        ResourceValidation passed = createResourceValidation(passedUrl, CheckStatus.PASSED);
        String failedUrl1 = "http://durgasoft.com";
        ResourceValidation failed1 = createResourceValidation(failedUrl1, CheckStatus.FAILED);
        String failedUrl2 = "http://example.com";
        ResourceValidation failed2 = createResourceValidation(failedUrl2, CheckStatus.FAILED);

        when(resourceValidator.perform(passed)).thenReturn(passed);
        when(resourceValidator.perform(failed1)).thenReturn(failed1);
        when(resourceValidator.perform(failed2)).thenReturn(failed2);

        steps.verifyHttpResources(
                new ExamplesTable(String.format("|url|%n|%s|%n|%s|%n|%s|%n", passedUrl, failedUrl1, failedUrl2)));

        verify(attachmentPublisher).publishAttachment("http-resources-validation-results.ftl",
                Map.of("results", List.of(failed1, failed2, passed)), "HTTP resources validation results");
    }

    private static ResourceValidation createResourceValidation(String url, CheckStatus status)
    {
        ResourceValidation validation = new ResourceValidation(URI.create(url));
        validation.setCheckStatus(status);
        return validation;
    }
}
