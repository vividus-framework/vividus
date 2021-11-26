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

package org.vividus.steps.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class NavigationStepsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private NavigationSteps steps;

    @Test
    void shouldNavigateBack()
    {
        WebDriver driver = mock(WebDriver.class);
        WebDriver.Navigation navigation = mock(WebDriver.Navigation.class);

        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.navigate()).thenReturn(navigation);

        steps.navigateBack();
        verify(navigation).back();
        verifyNoMoreInteractions(driver, navigation, webDriverProvider);
    }
}
