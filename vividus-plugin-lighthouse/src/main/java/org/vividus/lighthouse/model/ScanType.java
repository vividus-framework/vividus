/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.lighthouse.model;

public enum ScanType
{
    MOBILE(ScanType.MOBILE_STRATEGY),
    DESKTOP(ScanType.DESKTOP_STRATEGY),
    FULL(ScanType.DESKTOP_STRATEGY, ScanType.MOBILE_STRATEGY);

    private static final String MOBILE_STRATEGY = "mobile";
    private static final String DESKTOP_STRATEGY = "desktop";

    private final String[] strategies;

    ScanType(String... strategies)
    {
        this.strategies = strategies;
    }

    public String[] getStrategies()
    {
        return strategies;
    }
}
