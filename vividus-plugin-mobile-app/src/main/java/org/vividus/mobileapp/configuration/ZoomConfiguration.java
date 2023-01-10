/*
 * Copyright 2019-2022 the original author or authors.
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

public class ZoomConfiguration
{
    private final int topIndent;
    private final int bottomIndent;
    private final int rightIndent;
    private final int leftIndent;

    public ZoomConfiguration(int topIndent, int bottomIndent, int rightIndent, int leftIndent)
    {
        validateIndent("top", topIndent);
        validateIndent("bottom", bottomIndent);
        validateIndent("right", rightIndent);
        validateIndent("left", leftIndent);
        validateIndent("total vertical", topIndent + bottomIndent);
        validateIndent("total horizontal", rightIndent + leftIndent);
        this.topIndent = topIndent;
        this.bottomIndent = bottomIndent;
        this.rightIndent = rightIndent;
        this.leftIndent = leftIndent;
    }

    public int getTopIndent()
    {
        return topIndent;
    }

    public int getBottomIndent()
    {
        return bottomIndent;
    }

    public int getRightIndent()
    {
        return rightIndent;
    }

    public int getLeftIndent()
    {
        return leftIndent;
    }

    @SuppressWarnings("MagicNumber")
    private void validateIndent(String name, int value)
    {
        Validate.isTrue(value >= 0 && value <= 99,
                "The %s indent percentage value must be between 0 and 99, but got: %d", name, value);
    }
}
