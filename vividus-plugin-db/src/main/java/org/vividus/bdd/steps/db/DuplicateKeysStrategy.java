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

package org.vividus.bdd.steps.db;

public enum DuplicateKeysStrategy
{
    DISTINCT
    {
        @Override
        public int getTargetSize(DataSetComparisonRule comparisonRule, int leftSize, int rightSize)
        {
            return 1;
        }
    },
    NOOP
    {
        @Override
        public int getTargetSize(DataSetComparisonRule comparisonRule, int leftSize, int rightSize)
        {
            return comparisonRule == DataSetComparisonRule.CONTAINS ? rightSize : Math.max(leftSize, rightSize);
        }
    };

    public abstract int getTargetSize(DataSetComparisonRule comparisonRule, int leftSize, int rightSize);
}
