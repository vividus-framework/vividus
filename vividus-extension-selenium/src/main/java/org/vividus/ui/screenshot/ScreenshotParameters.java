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

package org.vividus.ui.screenshot;

import java.util.Optional;

public class ScreenshotParameters
{
    private int nativeFooterToCut;
    private Optional<String> shootingStrategy = Optional.empty();

    public int getNativeFooterToCut()
    {
        return nativeFooterToCut;
    }

    public void setNativeFooterToCut(int nativeFooterToCut)
    {
        this.nativeFooterToCut = nativeFooterToCut;
    }

    public Optional<String> getShootingStrategy()
    {
        return shootingStrategy;
    }

    public void setShootingStrategy(Optional<String> shootingStrategy)
    {
        this.shootingStrategy = shootingStrategy;
    }
}
