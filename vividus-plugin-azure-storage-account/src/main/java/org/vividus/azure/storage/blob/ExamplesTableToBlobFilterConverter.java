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

import static org.apache.commons.lang3.Validate.isTrue;

import java.lang.reflect.Type;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.vividus.steps.StringComparisonRule;

public class ExamplesTableToBlobFilterConverter extends AbstractParameterConverter<ExamplesTable, BlobFilter>
{
    @Override
    public BlobFilter convertValue(ExamplesTable table, Type type)
    {
        int rowCount = table.getRowCount();
        isTrue(rowCount == 1, "Exactly one row is expected in ExamplesTable representing blob filter, but found %d",
                rowCount);

        Parameters row = table.getRowAsParameters(0, true);
        String blobNamePrefix = row.valueAs("blobNamePrefix", String.class, null);
        StringComparisonRule blobNameFilterRule = row.valueAs("blobNameFilterRule", StringComparisonRule.class, null);
        String blobNameFilterValue = row.valueAs("blobNameFilterValue", String.class, null);
        Integer resultsLimit = row.valueAs("resultsLimit", Integer.class, null);

        isTrue((blobNameFilterRule == null) == (blobNameFilterValue == null),
                "'blobNameFilterRule' and 'blobNameFilterValue' must be specified in conjunction");
        isTrue(blobNamePrefix != null || blobNameFilterRule != null || resultsLimit != null,
                "At least one filter must be specified");

        Optional<Matcher<String>> blobNameMatcher = Optional.ofNullable(blobNameFilterRule)
                .map(r -> r.createMatcher(blobNameFilterValue));

        return new BlobFilter(Optional.ofNullable(blobNamePrefix), blobNameMatcher, Optional.ofNullable(resultsLimit));
    }
}
