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

package org.vividus.applitools.executioncloud;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.applitools.eyes.selenium.Eyes;

import org.vividus.selenium.RemoteWebDriverUrlProvider;

public class ExecutionCloudUrlProvider implements RemoteWebDriverUrlProvider
{
    @Override
    public URL getRemoteDriverUrl()
    {
        try
        {
            return new URL(Eyes.getExecutionCloudURL());
        }
        catch (MalformedURLException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
