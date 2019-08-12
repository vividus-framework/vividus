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

package org.vividus.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.util.UriUtils.UserInfo;

class UriUtilsTests
{
    private static final String SCHEME = "http";
    private static final String SCHEME_SPLITTER = "://";
    private static final String USER = "user";
    private static final String PASSWORD = "pass";
    private static final String USER_INFO = USER + ":" + PASSWORD;
    private static final String AUTHORITY = "somehost:8080";
    private static final String PATH = "/path";
    private static final String BAD_PATH = "/pa | th";
    private static final String FRAGMENT = "#fragment";
    private static final String BAD_FRAGMENT = "#fra| gme nt";
    private static final String GOOD_QUERY = "?name=goodvalue&a=b";
    private static final String BAD_QUERY = "?name=bad|value&a=b";
    private static final String ENCODED_BAD_QUERY = "?name=bad%7Cvalue&a=b";
    private static final String AT = "@";

    private static final String SERVER_URI_STR = SCHEME + SCHEME_SPLITTER + AUTHORITY;
    private static final String BASE_URI_STR = SERVER_URI_STR + PATH;
    private static final String BASE_URI_BAD_QUERY_STR = BASE_URI_STR + BAD_QUERY + FRAGMENT;
    private static final String URI_WITH_ENCODED_QUERY = BASE_URI_STR + ENCODED_BAD_QUERY + FRAGMENT;
    private static final URI BASE_URI = URI.create(BASE_URI_STR);
    private static final URI URI_WITH_USER_INFO = URI.create(SCHEME + SCHEME_SPLITTER + USER_INFO + AT + AUTHORITY
            + PATH);

    @Test
    void testAddUserInfo()
    {
        URI newUri = UriUtils.addUserInfo(BASE_URI, USER_INFO);
        assertEquals(URI_WITH_USER_INFO, newUri);
    }

    @Test
    void testAddNullUserInfo()
    {
        URI newUri = UriUtils.addUserInfo(BASE_URI, null);
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testAddUserInfoIfNotSet()
    {
        URI newUri = UriUtils.addUserInfoIfNotSet(BASE_URI, USER_INFO);
        assertEquals(URI_WITH_USER_INFO, newUri);
    }

    @Test
    void testDoNotAddUserInfoIfSet()
    {
        URI newUri = UriUtils.addUserInfoIfNotSet(URI_WITH_USER_INFO, USER_INFO);
        assertEquals(URI_WITH_USER_INFO, newUri);
    }

    @Test
    void testIsFromTheSameSiteSameUrisWithUserInfo()
    {
        assertTrue(UriUtils.isFromTheSameSite(URI_WITH_USER_INFO, URI_WITH_USER_INFO));
    }

    @Test
    void testIsFromTheSameSiteSameUrisWithoutUserInfo()
    {
        assertTrue(UriUtils.isFromTheSameSite(BASE_URI, BASE_URI));
    }

    @Test
    void testIsFromTheSameSiteWithoutUserInfo()
    {
        assertTrue(UriUtils.isFromTheSameSite(BASE_URI, URI_WITH_USER_INFO));
    }

    @Test
    void testIsFromTheSameSiteWithUserInfo()
    {
        assertTrue(UriUtils.isFromTheSameSite(URI_WITH_USER_INFO, BASE_URI));
    }

    @Test
    void testRemoveUserInfo()
    {
        URI newUri = UriUtils.removeUserInfo(URI_WITH_USER_INFO);
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testRemoveUnexistentUserInfo()
    {
        URI newUri = UriUtils.removeUserInfo(BASE_URI);
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testRemoveQuery()
    {
        URI newUri = UriUtils.removeQuery(URI.create(BASE_URI_STR + GOOD_QUERY));
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testRemoveUnexistentQuery()
    {
        URI newUri = UriUtils.removeQuery(BASE_URI);
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testRemoveFragment()
    {
        URI newUri = UriUtils.removeFragment(URI.create(BASE_URI_STR + FRAGMENT));
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testRemoveUnexistentFragment()
    {
        URI newUri = UriUtils.removeUserInfo(BASE_URI);
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testCreateUri()
    {
        String baseUriGoodQueryStr = BASE_URI_STR + GOOD_QUERY + FRAGMENT;
        URI newUri = UriUtils.createUri(baseUriGoodQueryStr);
        assertEquals(baseUriGoodQueryStr, newUri.toString());
    }

    @Test
    void testCreateBadUri()
    {
        String baseBadUri = SERVER_URI_STR + BAD_PATH + BAD_QUERY + BAD_FRAGMENT;
        URI newUri = UriUtils.createUri(baseBadUri);
        String expected = SERVER_URI_STR + "/pa%20%7C%20th" + ENCODED_BAD_QUERY + "#fra%7C%20gme%20nt";
        assertEquals(expected, newUri.toString());
    }

    @Test
    void testCreateUriBadQuery()
    {
        URI newUri = UriUtils.createUri(BASE_URI_BAD_QUERY_STR);
        assertEquals(URI_WITH_ENCODED_QUERY, newUri.toString());
    }

    @Test
    void testCreateUriDoubleEncoding()
    {
        URI newUri = UriUtils.createUri(UriUtils.createUri(BASE_URI_BAD_QUERY_STR).toString());
        assertEquals(URI_WITH_ENCODED_QUERY, newUri.toString());
    }

    @Test
    void testCreateUriNoQuery()
    {
        URI newUri = UriUtils.createUri(BASE_URI_STR);
        assertEquals(BASE_URI, newUri);
    }

    @Test
    void testCreateUriWithException()
    {
        String invalidUrl = "qwerty";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> UriUtils.createUri(invalidUrl));
        assertEquals("Scheme is missing in URL: " + invalidUrl, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        // CHECKSTYLE:OFF
        "http://somehost:8080/path,        /newPath,                                     http://somehost:8080/newPath",
        "http://somehost:8080/path,        /newPath?name1=value1&name2=value2#fragement, http://somehost:8080/newPath?name1=value1&name2=value2#fragement",
        "https://www.somehost.by//cookies, //path/extra-path/extra-extra-path,           https://www.somehost.by//path/extra-path/extra-extra-path",
        "https://www.somehost.by/,         /////crazy-url-path,                          https://www.somehost.by/////crazy-url-path",
        "tel:1234567,                      '',                                           tel:1234567"
        // CHECKSTYLE:ON
    })
    void testBuildNewUri(String baseUrl, String relativeUrl, String expectedUrl)
    {
        URI newUri = UriUtils.buildNewUrl(baseUrl, relativeUrl);
        assertEquals(expectedUrl, newUri.toString());
    }

    @ParameterizedTest
    @CsvSource({
        // CHECKSTYLE:OFF
        "http://somehost:8080/path,        /newPath,                                     http://somehost:8080/newPath",
        "http://somehost:8080/path/,       /newPath,                                     http://somehost:8080/newPath",
        "http://somehost:8080/path/,       newPath,                                      http://somehost:8080/path/newPath"
        // CHECKSTYLE:ON
    })
    void testBuildNewRelativeUri(String baseUrl, String relativeUrl, String expectedUrl) throws URISyntaxException
    {
        URI base = new URI(baseUrl);
        URI newUri = UriUtils.buildNewRelativeUrl(base, relativeUrl);
        assertEquals(expectedUrl, newUri.toString());
    }

    @Test
    void testBuildNewUriWithInvalidPath()
    {
        String newPath = "newPath";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> UriUtils.buildNewUrl(BASE_URI_STR, newPath));
        assertEquals("Relative path in absolute URI: " + SERVER_URI_STR + newPath, exception.getMessage());
    }

    @Test
    void testGetUserInfoFromUri()
    {
        UserInfo userInfo = UriUtils.getUserInfo(URI_WITH_USER_INFO);
        assertEquals(USER, userInfo.getUser());
        assertEquals(PASSWORD, userInfo.getPassword());
    }

    @Test
    void testGetUserInfoFromUriNotContainingUserInfo()
    {
        assertNull(UriUtils.getUserInfo(BASE_URI));
    }

    @Test
    void testRemoveRawUserInfo()
    {
        URI newUri = UriUtils.removeUserInfo(URI.create(SCHEME + SCHEME_SPLITTER + USER + ":R9%5EX" + AT + AUTHORITY
                + PATH));
        assertNull(newUri.getUserInfo());
    }

    @Test
    void testAddNotEncodedUserInfo()
    {
        assertNotNull(UriUtils.addUserInfo(BASE_URI, "user:R9^X").getUserInfo());
    }
}
