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

import com.saucelabs.saucerest.DataCenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataCenterFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCenterFactory.class);

    private DataCenterFactory()
    {
    }

    public static DataCenter createDataCenter(String dataCenterAsString)
    {
        DataCenter replacement = null;
        if ("EU".equalsIgnoreCase(dataCenterAsString))
        {
            replacement = DataCenter.EU_CENTRAL;
        }
        else if ("US".equalsIgnoreCase(dataCenterAsString))
        {
            replacement = DataCenter.US_WEST;
        }
        if (replacement != null)
        {
            LOGGER.atWarn()
                    .addArgument(dataCenterAsString)
                    .addArgument(replacement)
                    .log("SauceLabs data center '{}' is deprecated and its support will be removed in VIVIDUS 0.6.0,"
                            + " use data center '{}' instead");
            return replacement;
        }
        return DataCenter.fromString(dataCenterAsString);
    }
}
