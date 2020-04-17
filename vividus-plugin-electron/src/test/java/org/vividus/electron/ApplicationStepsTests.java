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

package org.vividus.electron;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.ui.web.SetContextSteps;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class ApplicationStepsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private SetContextSteps setContextSteps;

    @InjectMocks private ApplicationSteps applicationSteps;

    @Test
    void shouldStartElectronApplication()
    {
        applicationSteps.startApplication();
        verify(webDriverProvider).get();
        verifyNoInteractions(setContextSteps);
    }
}
