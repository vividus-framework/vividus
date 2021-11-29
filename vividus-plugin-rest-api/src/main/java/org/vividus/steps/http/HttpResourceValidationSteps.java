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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.http.validation.ResourceValidator;
import org.vividus.http.validation.model.ResourceValidation;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.util.UriUtils;

public class HttpResourceValidationSteps
{
    private final ResourceValidator<ResourceValidation> resourceValidator;
    private final AttachmentPublisher attachmentPublisher;

    public HttpResourceValidationSteps(ResourceValidator<ResourceValidation> resourceValidator,
            AttachmentPublisher attachmentPublisher)
    {
        this.resourceValidator = resourceValidator;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * The step executes HTTP HEAD request against each passed URL with the following conditions:
     * <ul>
     * <li>If the status code is acceptable then the check is considered as passed</li>
     * <li>If the status code is not acceptable but one of 404, 405, 501, 503 then the GET request will be sent</li>
     * <li>If the GET status code is acceptable then check is considered as passed, otherwise failed</li>
     * </ul>
     * @param resources The URLs of HTTP resources to validate
     */
    @Then("HTTP resources are valid:$resources")
    public void verifyHttpResources(ExamplesTable resources)
    {
        List<ResourceValidation> validations = resources.getRows()
                                                        .stream()
                                                        .map(row -> row.get("url"))
                                                        .map(UriUtils::createUri)
                                                        .map(ResourceValidation::new)
                                                        .map(resourceValidator::perform)
                                                        .sorted()
                                                        .collect(Collectors.toList());

        attachmentPublisher.publishAttachment("http-resources-validation-results.ftl", Map.of("results", validations),
                "HTTP resources validation results");
    }
}
