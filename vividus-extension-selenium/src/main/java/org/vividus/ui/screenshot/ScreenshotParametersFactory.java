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

public interface ScreenshotParametersFactory<C extends ScreenshotConfiguration>
{
    Optional<ScreenshotParameters> create(Optional<C> screenshotConfiguration);

    Optional<ScreenshotParameters> create(Map<IgnoreStrategy, Set<Locator>> ignores);
}
