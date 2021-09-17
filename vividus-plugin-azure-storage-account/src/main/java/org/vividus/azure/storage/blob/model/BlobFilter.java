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

package org.vividus.azure.storage.blob.model;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Optional;

import org.jbehave.core.annotations.AsParameters;
import org.vividus.bdd.steps.StringComparisonRule;

@AsParameters
public class BlobFilter
{
    private Optional<String> blobNamePrefix = Optional.empty();
    private Optional<StringComparisonRule> blobNameFilterRule = Optional.empty();
    private Optional<String> blobNameFilterValue = Optional.empty();
    private Optional<Long> resultsLimit = Optional.empty();

    public Optional<String> getBlobNamePrefix()
    {
        return blobNamePrefix;
    }

    public void setBlobNamePrefix(Optional<String> blobNamePrefix)
    {
        this.blobNamePrefix = blobNamePrefix;
    }

    public Optional<StringComparisonRule> getBlobNameFilterRule()
    {
        return blobNameFilterRule;
    }

    public void setBlobNameFilterRule(Optional<StringComparisonRule> blobNameFilterRule)
    {
        this.blobNameFilterRule = blobNameFilterRule;
    }

    public Optional<String> getBlobNameFilterValue()
    {
        return blobNameFilterValue;
    }

    public void setBlobNameFilterValue(Optional<String> blobNameFilterValue)
    {
        this.blobNameFilterValue = blobNameFilterValue;
    }

    public Optional<Long> getResultsLimit()
    {
        return resultsLimit;
    }

    public void setResultsLimit(Optional<Long> resultsLimit)
    {
        this.resultsLimit = resultsLimit;
    }

    public void validate()
    {
        isTrue(getResultsLimit().isPresent() || getBlobNameFilterValue().isPresent()
                        || getBlobNameFilterRule().isPresent() || getBlobNamePrefix().isPresent(),
                "At least one filter must be present");
        isTrue(getBlobNameFilterValue().isPresent() == getBlobNameFilterRule().isPresent(),
                "Should be specified together: 'blobNameFilterRle' and 'blobNameFilterValue'");
    }
}
