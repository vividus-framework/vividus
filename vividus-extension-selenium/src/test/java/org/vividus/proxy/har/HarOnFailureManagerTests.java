/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.proxy.har;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarCreatorBrowser;
import com.browserup.harreader.model.HarEntry;
import com.browserup.harreader.model.HarLog;
import com.browserup.harreader.model.HarPage;
import com.browserup.harreader.model.HarPostData;
import com.browserup.harreader.model.HarPostDataParam;
import com.browserup.harreader.model.HarQueryParam;
import com.browserup.harreader.model.HarRequest;
import com.browserup.harreader.model.HarResponse;
import com.browserup.harreader.model.HttpMethod;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.proxy.IProxy;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class HarOnFailureManagerTests
{
    private static final String URL = "www.test.com";
    private static final String KEY1 = "key1";
    private static final String VALUE1 = "value1";
    private static final String KEY2 = "key2";
    private static final String VALUE2 = "value2";
    private static final String MIME_TYPE = "mimeType";
    private static final String TEXT = "text";
    private static final String ENTRY_DATA_KEY = "lastHarEntryData";

    @Mock private IProxy proxy;
    @Mock private TestContext testContext;
    @InjectMocks private HarOnFailureManager harManager;

    @Test
    void shouldTakeHar()
    {
        Har harMock = mockHar();
        Optional<Har> har = harManager.takeHar();

        assertNotNull(har);
        assertEquals(harMock, har.get());
    }

    @Test
    void shouldTakeHarWithUniqueEntries()
    {
        List<HarEntry> entries = new ArrayList<>();
        List<HarPage> pages = createHarPages();
        Har har = createHar(entries);
        har.getLog().setPages(pages);
        entries.add(createHarEntry());
        entries.add(createHarEntry());
        Date newDate = new Date();
        newDate.setMinutes(new Date().getMinutes() - 1);
        entries.get(0).setStartedDateTime(newDate);
        when(testContext.get(ENTRY_DATA_KEY)).thenReturn(har.getLog().getEntries().get(1).getStartedDateTime());
        when(proxy.getRecordedData()).thenReturn(har);
        Optional<Har> newHar = harManager.takeHar();

        assertNotNull(newHar);
        assertNotEquals(newHar.get(), har);
    }

    private Har mockHar()
    {
        HarEntry harEntry = createHarEntry();
        Har har = createHar(List.of(harEntry));
        when(proxy.getRecordedData()).thenReturn(har);
        return har;
    }

    private Har createHar(List<HarEntry> entries)
    {
        HarLog harLog = new HarLog();
        HarCreatorBrowser browser = new HarCreatorBrowser();
        browser.setName("chrome");
        browser.setVersion("66");
        harLog.setBrowser(browser);
        harLog.setCreator(browser);
        harLog.setEntries(entries);
        Har har = new Har();
        har.setLog(harLog);
        return har;
    }

    private HarEntry createHarEntry()
    {
        HarPostData postData = new HarPostData();
        postData.setMimeType(MIME_TYPE);
        postData.setText(TEXT);
        postData.setParams(List.of(
                createHarPostDataParam(KEY1, VALUE1),
                createHarPostDataParam(KEY1, VALUE2),
                createHarPostDataParam(KEY2, VALUE2)
        ));

        HarRequest request = new HarRequest();
        request.setMethod(HttpMethod.POST);
        request.setUrl(URL);
        request.setQueryString(List.of(
                createHarQueryParam(KEY1, VALUE1),
                createHarQueryParam(KEY1, VALUE2),
                createHarQueryParam(KEY2, VALUE2)
        ));
        request.setPostData(postData);

        HarResponse response = new HarResponse();
        response.setStatus(HttpStatus.SC_OK);

        HarEntry harEntry = new HarEntry();
        harEntry.setRequest(request);
        harEntry.setResponse(response);
        return harEntry;
    }

    private HarQueryParam createHarQueryParam(String key, String value)
    {
        HarQueryParam harQueryParam = new HarQueryParam();
        harQueryParam.setName(key);
        harQueryParam.setValue(value);
        return harQueryParam;
    }

    private HarPostDataParam createHarPostDataParam(String key, String value)
    {
        HarPostDataParam postDataParam = new HarPostDataParam();
        postDataParam.setName(key);
        postDataParam.setValue(value);
        return postDataParam;
    }

    private List<HarPage> createHarPages()
    {
        List<HarPage> pages = new ArrayList<>();
        HarPage harPage = new HarPage();
        harPage.setTitle("Page 0");
        HarPage harPage1 = new HarPage();
        harPage.setTitle("Page 1");
        Date newDate = new Date();
        newDate.setMinutes(new Date().getMinutes() - 1);
        harPage1.setStartedDateTime(newDate);
        pages.add(harPage);
        pages.add(harPage1);
        return pages;
    }
}
