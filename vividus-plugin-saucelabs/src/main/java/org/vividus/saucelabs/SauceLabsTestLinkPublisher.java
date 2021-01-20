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

package org.vividus.saucelabs;

import static java.lang.String.format;

import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.saucelabs.saucerest.DataCenter;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestLinkPublisher;
import org.vividus.testcontext.TestContext;

public class SauceLabsTestLinkPublisher extends AbstractCloudTestLinkPublisher
{
    private final String sauceLabsUrl;

    public SauceLabsTestLinkPublisher(DataCenter dataCenter, IWebDriverProvider webDriverProvider, EventBus eventBus,
            TestContext testContext)
    {
        super("SauceLabs", webDriverProvider, eventBus, testContext);
        this.sauceLabsUrl = dataCenter.appServer();
    }

    @Override
    protected Optional<String> getCloudTestUrl(String sessionId)
    {
        return Optional.of(format("%stests/%s", sauceLabsUrl, sessionId));
    }
}
