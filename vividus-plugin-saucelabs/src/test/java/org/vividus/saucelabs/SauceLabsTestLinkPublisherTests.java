/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.saucelabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.saucelabs.saucerest.DataCenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.MutableCapabilities;
import org.vividus.selenium.cloud.AbstractCloudTestLinkPublisher.GetCloudTestUrlException;
import org.vividus.selenium.manager.IGenericWebDriverManager;

@ExtendWith(MockitoExtension.class)
class SauceLabsTestLinkPublisherTests
{
    @Mock private IGenericWebDriverManager webDriverManager;
    private SauceLabsTestLinkPublisher linkPublisher;

    @BeforeEach
    void init()
    {
        this.linkPublisher = new SauceLabsTestLinkPublisher(DataCenter.EU_CENTRAL, null, null, null,
                webDriverManager);
    }

    @Test
    void shouldReturnSessionUrl() throws GetCloudTestUrlException
    {
        when(webDriverManager.getCapabilities()).thenReturn(new MutableCapabilities());
        assertEquals("https://app.eu-central-1.saucelabs.com/tests/session-id",
                linkPublisher.getCloudTestUrl("session-id"));
    }

    @Test
    void shouldReturnSessionUrlForTestObject() throws GetCloudTestUrlException
    {
        String testObjectUrl = "https://app.saucelabs.com/tests/id";
        when(webDriverManager.getCapabilities()).thenReturn(new MutableCapabilities(Map.of(
            "appium:testobject_test_report_url", testObjectUrl
        )));
        assertEquals(testObjectUrl, linkPublisher.getCloudTestUrl(null));
    }
}
