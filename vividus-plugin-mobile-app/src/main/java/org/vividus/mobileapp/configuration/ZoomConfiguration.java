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

package org.vividus.mobileapp.configuration;

import org.apache.commons.lang3.Validate;

public record ZoomConfiguration(int topIndent, int bottomIndent, int rightIndent, int leftIndent)
{
    public ZoomConfiguration
    {
        validateIndent("top", topIndent);
        validateIndent("bottom", bottomIndent);
        validateIndent("right", rightIndent);
        validateIndent("left", leftIndent);
        validateIndent("total vertical", topIndent + bottomIndent);
        validateIndent("total horizontal", rightIndent + leftIndent);
    }

    @SuppressWarnings("MagicNumber")
    private void validateIndent(String name, int value)
    {
        Validate.isTrue(value >= 0 && value <= 99,
                "The %s indent percentage value must be between 0 and 99, but got: %d", name, value);
    }
}
