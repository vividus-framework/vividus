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

package org.vividus.proxy;

import static java.util.stream.Collectors.toList;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import org.vividus.http.HttpMethod;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarContent;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarNameVersion;
import net.lightbody.bmp.core.har.HarRequest;

public class ProxyLog
{
    private static final String HEX40 = "HEX40";

    private final Har har;

    public ProxyLog(Har har)
    {
        this.har = har;
    }

    /**
     * Clear log by removing all entries from the log
     */
    public void clear()
    {
        HarNameVersion creator = har.getLog().getCreator();
        har.setLog(new HarLog(creator));
    }

    /**
     * Get list of request URL-s
     * @return List of URLs
     */
    public List<String> getRequestUrls()
    {
        return getRequestUrlStream().collect(toList());
    }

    /**
     * Get list of request URL-s by pattern
     * @param urlPattern Pattern of the URL
     * @return List of URLs that are matched the URL pattern
     */
    public List<String> getRequestUrls(String urlPattern)
    {
        return getRequestUrlStream().filter(url -> url.matches(urlPattern)).collect(toList());
    }

    /**
     * Get first request URL by pattern
     * @param urlPattern Pattern of the URL
     * @return Requested URL that is matched the URL pattern
     */
    public String getRequestUrl(String urlPattern)
    {
        List<String> urls = getRequestUrls(urlPattern);
        if (urls.isEmpty())
        {
            throw new IllegalArgumentException("Request URL is not found by pattern: " + urlPattern);
        }
        return urls.get(0);
    }

    /**
     * Get collection of HAR entries
     * @return List of HAR entries
     */
    public List<HarEntry> getLogEntries()
    {
        return har.getLog().getEntries();
    }

    /**
     * Get collection of HAR entries filtered by URL pattern
     * @param urlPattern Pattern of the URL
     * @return List of responses found by URL pattern
     */
    public List<HarEntry> getLogEntries(String urlPattern)
    {
        return getFilteredHarEntriesStream(urlPattern).collect(toList());
    }

    /**
     * Get collection of HAR entries filtered by HTTP method and URL pattern
     * @param httpMethod HTTP method
     * @param urlPattern Pattern of the URL
     * @return List of responses found by URL pattern
     */
    public List<HarEntry> getLogEntries(HttpMethod httpMethod, String urlPattern)
    {
        String httpMethodStr = httpMethod.toString();
        return getFilteredHarEntriesStream(urlPattern)
                .filter(entry -> httpMethodStr.equals(entry.getRequest().getMethod())).collect(toList());
    }

    /**
     * Get list of responses as list of <code>java.lang.String</code>
     * @return List of responses
     */
    public List<String> getResponses()
    {
        return getHarEntriesStream().map(ProxyLog::getResponse).collect(toList());
    }

    /**
     * Get list of responses as <code>java.lang.String</code> found by URL pattern
     * @param urlPattern Pattern of the URL
     * @return List of responses found by URL pattern
     */
    public List<String> getResponses(String urlPattern)
    {
        return getFilteredHarEntriesStream(urlPattern).map(ProxyLog::getResponse).collect(toList());
    }

    /**
     * Get first response as <code>java.lang.String</code> found by URL pattern
     * @param urlPattern Pattern of the URL
     * @return Response that is matched the URL pattern
     */
    public String getResponse(String urlPattern)
    {
        List<String> responses = getResponses(urlPattern);
        if (responses.isEmpty())
        {
            throw new IllegalArgumentException("Response is not found by URL pattern: " + urlPattern);
        }
        return responses.get(0);
    }

    private Stream<HarEntry> getHarEntriesStream()
    {
        return getLogEntries().stream();
    }

    private Stream<String> getRequestUrlStream()
    {
        return getHarEntriesStream().map(HarEntry::getRequest).map(HarRequest::getUrl).map(ProxyLog::decodeUrl);
    }

    private static String getResponse(HarEntry entry)
    {
        HarContent content = entry.getResponse().getContent();
        String response = content.getText();

        if (response != null && !content.getMimeType().startsWith("text"))
        {
            response = new String(Base64.getDecoder().decode(response), StandardCharsets.UTF_8);
        }
        return response;
    }

    private Stream<HarEntry> getFilteredHarEntriesStream(String urlPattern)
    {
        return getHarEntriesStream().filter(entry -> decodeUrl(entry.getRequest().getUrl()).matches(urlPattern));
    }

    private static String decodeUrl(String encodedUrl)
    {
        return encodedUrl.contains(HEX40) ? encodedUrl.split(HEX40)[0] : encodedUrl;
    }
}
