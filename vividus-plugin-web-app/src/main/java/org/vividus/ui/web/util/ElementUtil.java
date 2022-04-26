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

package org.vividus.ui.web.util;

import org.openqa.selenium.WebElement;

public class ElementUtil
{
    public static final int ACCURACY = 2;
    public static final double HUNDRED = 100;

    public static long getElementWidthInPerc(WebElement parent, WebElement element)
    {
        double elementWidth = element.getSize().getWidth();
        double parentWidth = parent.getSize().getWidth();
        return Math.round(elementWidth / parentWidth * HUNDRED);
    }
}
