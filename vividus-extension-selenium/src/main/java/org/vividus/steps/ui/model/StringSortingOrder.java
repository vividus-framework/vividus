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

package org.vividus.steps.ui.model;

import java.util.Collections;
import java.util.Comparator;

public enum StringSortingOrder
{
    ASCENDING(Comparator.naturalOrder()),
    DESCENDING(Comparator.reverseOrder()),
    CASE_INSENSITIVE_ASCENDING(String.CASE_INSENSITIVE_ORDER),
    CASE_INSENSITIVE_DESCENDING(Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));

    private final Comparator<String> sortingType;

    StringSortingOrder(Comparator<String> sortingType)
    {
        this.sortingType = sortingType;
    }

    public Comparator<String> getSortingType()
    {
        return sortingType;
    }
}
