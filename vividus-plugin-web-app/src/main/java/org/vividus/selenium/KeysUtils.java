/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.selenium;

import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.openqa.selenium.Keys;

public final class KeysUtils
{
    private KeysUtils()
    {

    }

    public static CharSequence[] keysToCharSequenceArray(List<String> keys)
    {
        return keys.stream().map(key -> EnumUtils.isValidEnum(Keys.class, key) ? Keys.valueOf(key) : key)
                .toArray(CharSequence[]::new);
    }
}
