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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public final class UriUtils
{
    private static final String EMPTY = "";
    private static final char SCHEME_SEPARATOR = ':';
    private static final String USER_INFO_SEPARATOR = "@";
    private static final String QUERY_SEPARATOR = "?";
    private static final String FRAGMENT_SEPARATOR = "#";
    private static final char SLASH = '/';

    private UriUtils()
    {
    }

    public static URI addUserInfo(URI uri, String userInfo)
    {
        if (userInfo != null)
        {
            String host = uri.getHost();
            return createUri(uri.toString().replace(host, userInfo + USER_INFO_SEPARATOR + host));
        }
        return uri;
    }

    public static URI addUserInfoIfNotSet(URI url, String userInfo)
    {
        if (url.getUserInfo() == null)
        {
            return addUserInfo(url, userInfo);
        }
        return url;
    }

    public static boolean isFromTheSameSite(URI siteUri, URI uriToCheck)
    {
        return removeUserInfo(siteUri).getAuthority().equals(removeUserInfo(uriToCheck).getAuthority());
    }

    public static UserInfo getUserInfo(URI uri)
    {
        return Optional.ofNullable(uri.getUserInfo()).map(UriUtils::parseUserInfo).orElse(null);
    }

    public static UserInfo parseUserInfo(String userInfo)
    {
        int indexOfColon = userInfo.indexOf(':');
        String user = userInfo.substring(0, indexOfColon);
        String password = userInfo.substring(indexOfColon + 1);
        return new UserInfo(user, password);
    }

    public static URI removeUserInfo(URI uri)
    {
        return removePart(uri, uri::getRawUserInfo, EMPTY, USER_INFO_SEPARATOR);
    }

    public static URI removeQuery(URI uri)
    {
        return removePart(uri, uri::getRawQuery, QUERY_SEPARATOR, EMPTY);
    }

    public static URI removeFragment(URI uri)
    {
        return removePart(uri, uri::getFragment, FRAGMENT_SEPARATOR, EMPTY);
    }

    private static URI removePart(URI uri, Supplier<String> partSupplier, String startSeparator, String endSeparator)
    {
        String part = partSupplier.get();
        if (part != null)
        {
            return URI.create(uri.toString().replace(startSeparator + part + endSeparator, EMPTY));
        }
        return uri;
    }

    public static URI createUri(String url)
    {
        try
        {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name());

            int schemeSeparatorIndex = decodedUrl.indexOf(SCHEME_SEPARATOR);
            if (schemeSeparatorIndex < 0)
            {
                throw new IllegalArgumentException("Scheme is missing in URL: " + url);
            }
            String scheme = decodedUrl.substring(0, schemeSeparatorIndex);

            if (scheme.startsWith("http"))
            {
                URL uri = new URL(decodedUrl);
                return new URI(uri.getProtocol(), uri.getAuthority(), uri.getPath(), uri.getQuery(), uri.getRef());
            }

            char fragmentSeparator = '#';
            int fragmentSeparatorIndex = decodedUrl.lastIndexOf(fragmentSeparator);
            String fragment = null;
            if (fragmentSeparatorIndex < 0)
            {
                fragmentSeparatorIndex = decodedUrl.length();
            }
            else
            {
                fragment = decodedUrl.substring(fragmentSeparatorIndex + 1);
            }

            URI uri = new URI(scheme, decodedUrl.substring(0, fragmentSeparatorIndex), fragment);
            StringBuilder result = new StringBuilder(uri.getRawSchemeSpecificPart());
            if (fragment != null)
            {
                result.append(fragmentSeparator).append(uri.getRawFragment());
            }
            return URI.create(result.toString());
        }
        catch (UnsupportedEncodingException | URISyntaxException | MalformedURLException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public static URI buildNewUrl(String url, String relativeUrl)
    {
        return buildNewUrl(createUri(url), relativeUrl);
    }

    public static URI buildNewUrl(URI url, String relativeUrl)
    {
        int indexOfFirstNonSlashChar = StringUtils.indexOfAnyBut(relativeUrl, SLASH);
        String normalizedRelativeUrl = indexOfFirstNonSlashChar > 1
                ? relativeUrl.substring(indexOfFirstNonSlashChar - 1)
                : relativeUrl;
        try
        {
            URI parsedRelativeUrl = URI.create(normalizedRelativeUrl);
            if (url.isOpaque())
            {
                return new URI(url.getScheme(), url.getSchemeSpecificPart(), parsedRelativeUrl.getFragment());
            }

            String path = StringUtils.repeat(SLASH, indexOfFirstNonSlashChar - 1) + parsedRelativeUrl.getPath();
            return new URI(url.getScheme(), url.getAuthority(), path, parsedRelativeUrl.getQuery(),
                    parsedRelativeUrl.getFragment());
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Builds a new URL from base URL and relative URL
     * <br>A <b>base URL</b> - an absolute URL (e.g <code>https://example.com/path</code>).
     * <br>A <b>relative URL</b> pointing to any resource (e.g <code>/other</code>)
     * <br>
     * Examples:
     * <pre>
     * buildNewRelativeUrl(new URI("https://example.com/path"), "/test")  --&gt; https://example.com/test
     * buildNewRelativeUrl(new URI("https://example.com/path/"), "/test") --&gt; https://example.com/test
     * buildNewRelativeUrl(new URI("https://example.com/path"), "test")   --&gt; https://example.com/path/test
     * </pre>
     * @param baseUri Base URL
     * @param relativeUrl A string value of the relative URL
     * @return new URL built from base URL and relative URL
     */
    public static URI buildNewRelativeUrl(URI baseUri, String relativeUrl)
    {
        String pathToGo = relativeUrl;
        if (!pathToGo.startsWith(String.valueOf(SLASH)))
        {
            String currentPath = FilenameUtils.getFullPath(baseUri.getPath());
            if (currentPath.isEmpty())
            {
                currentPath = String.valueOf(SLASH);
            }
            pathToGo = currentPath + pathToGo;
        }
        return buildNewUrl(baseUri, pathToGo);
    }

    public static final class UserInfo
    {
        private final String user;
        private final String password;

        public UserInfo(String user, String password)
        {
            this.user = user;
            this.password = password;
        }

        public String getUser()
        {
            return user;
        }

        public String getPassword()
        {
            return password;
        }
    }
}
