/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.ui.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class InternetUtilTests
{
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    static Stream<Arguments> testData()
    {
        return Stream.of(
            Arguments.of("http://test.topdomain.com/test/test", "topdomain.com"),
            Arguments.of("http://testproduct:80/test", "testproduct"),
            Arguments.of("http://127.0.0.1:8080", "127.0.0.1")
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    void testGetTopDomainURL(URL url, String expectedDomain)
    {
        assertEquals(expectedDomain, InternetUtils.getTopDomain(url));
    }

    @ParameterizedTest
    @MethodSource("testData")
    void testGetTopDomainURI(URI uri, String expectedDomain)
    {
        assertEquals(expectedDomain, InternetUtils.getTopDomain(uri));
    }

    @ParameterizedTest
    @CsvSource({
        "http://www.by.example.com,           2, example.com",
        "http://www.by.example.com,           5, www.by.example.com",
        "http://user:pass@www.by.example.com, 4, www.by.example.com"
    })
    void testGetUriDomainLevels(String uri, int domainLevels, String expectedDomainName)
    {
        assertEquals(expectedDomainName, InternetUtils.getDomainName(URI.create(uri), domainLevels));
    }
}
