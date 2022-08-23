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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;

public class ScreenshotParameters
{
    private int cutTop;
    private int cutBottom;
    private int cutLeft;
    private int cutRight;

    private Optional<String> shootingStrategy = Optional.empty();
    private Map<IgnoreStrategy, Set<Locator>> ignoreStrategies;

    public int getCutTop()
    {
        return cutTop;
    }

    public void setCutTop(int cutTop)
    {
        this.cutTop = cutTop;
    }

    public int getCutBottom()
    {
        return cutBottom;
    }

    public void setCutBottom(int cutBottom)
    {
        this.cutBottom = cutBottom;
    }

    public int getCutLeft()
    {
        return cutLeft;
    }

    public void setCutLeft(int cutLeft)
    {
        this.cutLeft = cutLeft;
    }

    public int getCutRight()
    {
        return cutRight;
    }

    public void setCutRight(int cutRight)
    {
        this.cutRight = cutRight;
    }

    public Optional<String> getShootingStrategy()
    {
        return shootingStrategy;
    }

    public void setShootingStrategy(Optional<String> shootingStrategy)
    {
        this.shootingStrategy = shootingStrategy;
    }

    public Map<IgnoreStrategy, Set<Locator>> getIgnoreStrategies()
    {
        return ignoreStrategies;
    }

    public void setIgnoreStrategies(Map<IgnoreStrategy, Set<Locator>> ignoreStrategies)
    {
        this.ignoreStrategies = ignoreStrategies;
    }
}
