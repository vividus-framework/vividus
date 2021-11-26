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

package org.vividus.steps.ui.web.model;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.vividus.util.EnumUtils.toHumanReadableForm;

public enum WebPerformanceMetric
{
    TIME_TO_FIRST_BYTE,
    DNS_LOOKUP_TIME,
    DOM_CONTENT_LOAD_TIME,
    PAGE_LOAD_TIME;

    @Override
    public String toString()
    {
        return capitalize(toHumanReadableForm(this));
    }
}
