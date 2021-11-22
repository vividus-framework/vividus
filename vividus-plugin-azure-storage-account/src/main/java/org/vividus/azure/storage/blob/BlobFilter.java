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

package org.vividus.azure.storage.blob;

import java.util.Optional;

import org.hamcrest.Matcher;

public class BlobFilter
{
    private final Optional<String> blobNamePrefix;
    private final Optional<Matcher<String>> blobNameMatcher;
    private final Optional<Integer> resultsLimit;

    public BlobFilter(Optional<String> blobNamePrefix, Optional<Matcher<String>> blobNameMatcher,
            Optional<Integer> resultsLimit)
    {
        this.blobNamePrefix = blobNamePrefix;
        this.blobNameMatcher = blobNameMatcher;
        this.resultsLimit = resultsLimit;
    }

    public Optional<String> getBlobNamePrefix()
    {
        return blobNamePrefix;
    }

    public Optional<Matcher<String>> getBlobNameMatcher()
    {
        return blobNameMatcher;
    }

    public Optional<Integer> getResultsLimit()
    {
        return resultsLimit;
    }
}
