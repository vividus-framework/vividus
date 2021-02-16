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

package org.vividus.http.validation.model;

public enum CheckStatus
{
    FAILED(1),
    BROKEN(2),
    PASSED(3),
    FILTERED(4),
    SKIPPED(5);

    private final int weight;

    CheckStatus(int weight)
    {
        this.weight = weight;
    }

    public static CheckStatus get(boolean passed)
    {
        return passed ? PASSED : FAILED;
    }

    public int getWeight()
    {
        return weight;
    }
}
