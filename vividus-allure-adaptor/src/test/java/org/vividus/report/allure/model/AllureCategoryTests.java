/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.report.allure.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.qameta.allure.entity.Status;

class AllureCategoryTests
{
    @Test
    void shouldParseAllureStatusesFromString()
    {
        AllureCategory category = new AllureCategory();
        category.setName("category");
        category.setStatuses("broken, FAILED");
        assertEquals(2, category.getMatchedStatuses().size());
        assertIterableEquals(List.of("broken", "failed"),
                category.getMatchedStatuses().stream().map(Status::value).collect(Collectors.toList()));
    }
}
