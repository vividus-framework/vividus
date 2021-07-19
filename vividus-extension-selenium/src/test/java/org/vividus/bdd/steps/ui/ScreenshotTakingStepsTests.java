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

package org.vividus.bdd.steps.ui;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.screenshot.ScreenshotTaker;

@ExtendWith(MockitoExtension.class)
class ScreenshotTakingStepsTests
{
    private static final String PATH = "path";

    @Mock
    private ScreenshotTaker screenshotTaker;

    @InjectMocks
    private ScreenshotTakingSteps screenshotTakingSteps;

    @Test
    void testWhenITakeScreenshot() throws IOException
    {
        screenshotTakingSteps.whenITakeScreenshot();
        verify(screenshotTaker).takeScreenshotAsFile("Step_Screenshot");
    }

    @Test
    void testWhenITakeScreenshotToPath() throws IOException
    {
        Path screenshotFilePath = Paths.get(PATH);
        screenshotTakingSteps.whenITakeScreenshotToPath(screenshotFilePath);
        verify(screenshotTaker).takeScreenshot(screenshotFilePath);
    }
}
