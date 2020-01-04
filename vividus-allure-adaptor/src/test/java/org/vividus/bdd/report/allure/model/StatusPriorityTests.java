/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.report.allure.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.qameta.allure.model.Status;

class StatusPriorityTests
{
    @Test
    void testGetLowest()
    {
        assertEquals(StatusPriority.NOT_COVERED, StatusPriority.getLowest());
    }

    @Test
    void testFromStatus()
    {
        assertEquals(StatusPriority.PASSED, StatusPriority.fromStatus(Status.PASSED));
    }
}
