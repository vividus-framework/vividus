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

package org.vividus.selenium.sauce;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
public class SauceLabsStepsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IBddRunContext bddRunContext;
    @InjectMocks private SauceLabsSteps sauceLabsSteps;

    @ParameterizedTest
    @CsvSource({"false, passed", "true, failed"})
    void shouldChangeSauceLabsStatusScenario(boolean isTestFailed, String result) throws WebDriverException
    {
        mockRunningScenario(isTestFailed);
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(remoteWebDriver);
        sauceLabsSteps.updateStatusAfterStory();
        verify(remoteWebDriver).executeScript("sauce:job-result=" + result);
    }

    @Test
    void shouldNotChangeStatusIfWebDriverWasClosed()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        sauceLabsSteps.updateStatusAfterStory();
        verifyNoInteractions(bddRunContext);
    }

    private void mockRunningScenario(boolean failed)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setFailed(failed);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
    }
}
