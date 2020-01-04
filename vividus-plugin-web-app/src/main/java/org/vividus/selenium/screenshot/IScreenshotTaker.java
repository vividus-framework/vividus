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

package org.vividus.selenium.screenshot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public interface IScreenshotTaker
{
    Optional<Screenshot> takeScreenshot(String screenshotName);

    Optional<Screenshot> takeScreenshot(String screenshotName, boolean viewportScreenshot);

    Optional<Screenshot> takeScreenshot(String screenshotName, List<WebElement> webElementsToHighlight);

    Optional<Screenshot> takeScreenshot(String screenshotName, List<WebElement> webElementsToHighlight,
            boolean viewportScreenshot);

    Path takeScreenshotAsFile(String screenshotName) throws IOException;

    void takeScreenshot(Path screenshotFilePath) throws IOException;

    void takeScreenshot(Path screenshotFilePath, boolean viewportScreenshot) throws IOException;

    Optional<Screenshot> takeScreenshot(String screenshotName, SearchContext searchContext);

    ru.yandex.qatools.ashot.Screenshot takeAshotScreenshot(SearchContext searchContext,
            Optional<ScreenshotConfiguration> screenshotConfiguration);
}
