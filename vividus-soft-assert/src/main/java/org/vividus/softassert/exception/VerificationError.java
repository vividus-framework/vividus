/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.softassert.exception;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;

public class VerificationError extends AssertionError
{
    private static final long serialVersionUID = -6109674223874299855L;
    private final List<SoftAssertionError> errors;

    public VerificationError(String message, List<SoftAssertionError> errors)
    {
        super(message);
        this.errors = errors;
    }

    public List<SoftAssertionError> getErrors()
    {
        return errors;
    }

    public Set<KnownIssue> getKnownIssues()
    {
        return errors.stream()
                .map(SoftAssertionError::getKnownIssue)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public boolean isOngoingKnownIssuesOnly()
    {
        return errors.stream().noneMatch(error -> !error.isKnownIssue() || error.getKnownIssue().isFixed());
    }
}
