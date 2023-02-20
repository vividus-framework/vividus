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

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.saucelabs.saucerest.DataCenter;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(TestLoggerFactoryExtension.class)
class DataCenterFactoryTests
{
    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(DataCenterFactory.class);

    @ParameterizedTest
    @CsvSource({
            "US_WEST,        US_WEST",
            "Us_East,        US_EAST",
            "eu_central,     EU_CENTRAL",
            "apac_SOUTHEAST, APAC_SOUTHEAST"
    })
    void parseDataCenter(String dataCenterAsString, DataCenter expected)
    {
        assertEquals(DataCenterFactory.createDataCenter(dataCenterAsString), expected);
        assertThat(testLogger.getLoggingEvents(), is(empty()));
    }

    @ParameterizedTest
    @CsvSource({
            "US, US_WEST",
            "us, US_WEST",
            "Eu, EU_CENTRAL",
            "EU, EU_CENTRAL"
    })
    void parseDeprecatedDataCenter(String dataCenterAsString, DataCenter expected)
    {
        assertEquals(DataCenterFactory.createDataCenter(dataCenterAsString), expected);
        assertThat(testLogger.getLoggingEvents(), is(List.of(
                warn("SauceLabs data center '{}' is deprecated and its support will be removed in VIVIDUS 0.6.0,"
                        + " use data center '{}' instead", dataCenterAsString, expected))));
    }
}
