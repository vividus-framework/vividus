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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InetAddressUtilsTests
{
    @ParameterizedTest
    @CsvSource({
            "https://www.domain.com, .domain.com",
            "http://127.0.0.1:8080, 127.0.0.1",
            "http://localhost:8080,  localhost"
    })
    void shouldGetDomain(String urlAsString, String domain)
    {
        assertEquals(domain, InetAddressUtils.getDomain(urlAsString));
    }

    @ParameterizedTest
    @CsvSource({
            "https://www.domain.com,                       domain.com,                              true",
            "https://www.domain.com,                       www.domain.com,                          true",
            "http://127.0.0.1:8080,                        127.0.0.1,                               true",
            "http://localhost:8080,                        localhost,                               true",
            "https://vividus-test-site-a92k.onrender.com/, vividus-test-site-a92k.onrender.com,     true",
            "https://vividus-test-site-a92k.onrender.com/, onrender.com,                            true",
            "https://vividus-test-site-a92k.onrender.com/, abc.vividus-test-site-a92k.onrender.com, false"
    })
    void shouldCheckIsSubDomain(String urlAsString, String domain, boolean expected)
    {
        assertEquals(expected, InetAddressUtils.isSubDomain(domain, urlAsString));
    }
}
