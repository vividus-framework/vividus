/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.mobitru.selenium;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobitruSeleniumConfigurationValidatorTests
{
    private static final String URL_FAILURE_MESSAGE = "The host specified in the URL property must be the same as"
            + " the value of the 'selenium.grid.host' property";

    @Test
    void shouldPassValidation() throws MalformedURLException
    {
        MobitruSeleniumConfigurationValidator configurationValidator = new MobitruSeleniumConfigurationValidator(
                "app.mobitru.com", new URL("https://personal:mobitru_ak_vvd@app.mobitru.com/wd/hub"));
        assertDoesNotThrow(configurationValidator::validate);
    }

    @ParameterizedTest
    @CsvSource({
        "localhost, https://personal:mobitru_ak_vvd@app.mobitru.com/wd/hub, host, localhost",
        ", https://personal:mobitru_ak_vvd@app.mobitru.com/wd/hub, host, null",
        "app.mobitru.com, https://localhost/wd/hub, url, " + URL_FAILURE_MESSAGE,
        "app.mobitru.com, , url, " + URL_FAILURE_MESSAGE
        })
    void shouldThrowException(String host, URL url, String failedPropertyName, String clarifyingMessage)
            throws MalformedURLException
    {
        MobitruSeleniumConfigurationValidator configurationValidator =
                new MobitruSeleniumConfigurationValidator(host, url);
        var exception = assertThrows(IllegalArgumentException .class, configurationValidator::validate);
        String expectedExceptionMessage =
                String.format("Incorrect 'selenium.grid.%s' property value for Mobitru: %s",
                        failedPropertyName, clarifyingMessage);
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }
}
