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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarEntry;
import com.browserup.harreader.model.HarLog;
import com.browserup.harreader.model.HarPage;

import org.vividus.proxy.IProxy;
import org.vividus.testcontext.TestContext;

public class HarOnFailureManager
{
    private static final String ENTRY_DATA_KEY = "lastHarEntryData";

    private final TestContext testContext;
    private final IProxy proxy;

    public HarOnFailureManager(IProxy proxy, TestContext testContext)
    {
        this.testContext = testContext;
        this.proxy = proxy;
    }

    public Optional<Har> takeHar()
    {
        return Optional.of(createHarWithUniqueData(proxy.getRecordedData()));
    }

    private Har createHarWithUniqueData(Har har)
    {
        Har newHar = copy(har);
        Date savedDateTime = testContext.get(ENTRY_DATA_KEY);
        if (null != savedDateTime)
        {
            newHar = removeValuesFromHarBeforeSelectedDate(newHar, savedDateTime);
            testContext.remove(ENTRY_DATA_KEY);
        }
        List<HarEntry> harEntries = har.getLog().getEntries();
        testContext.put(ENTRY_DATA_KEY, harEntries.get(harEntries.size() - 1).getStartedDateTime());
        return newHar;
    }

    private Har removeValuesFromHarBeforeSelectedDate(Har har, Date lastHarEntryDateTime)
    {
        List<HarEntry> selectedEntries = har.getLog().getEntries().stream()
                .filter(entry -> lastHarEntryDateTime.after(entry.getStartedDateTime()) || entry.getStartedDateTime()
                        .equals(lastHarEntryDateTime))
                .collect(Collectors.toList());
        har.getLog().getEntries().removeAll(selectedEntries);

        List<HarPage> selectedPages = har.getLog().getPages().stream()
                .filter(page -> lastHarEntryDateTime.after(page.getStartedDateTime()))
                .collect(Collectors.toList());

        har.getLog().getEntries().removeAll(selectedEntries);
        har.getLog().getPages().removeAll(selectedPages);

        return har;
    }

    private Har copy(Har proxyHar)
    {
        Har newHar = new Har();
        HarLog newHarLog = newHar.getLog();
        HarLog proxyHarLog = proxyHar.getLog();
        newHarLog.getEntries().addAll(proxyHarLog.getEntries());
        newHarLog.getPages().addAll(proxyHarLog.getPages());
        newHarLog.setBrowser(proxyHarLog.getBrowser());
        newHarLog.setVersion(proxyHarLog.getVersion());
        newHarLog.setComment(proxyHarLog.getComment());
        newHarLog.setCreator(proxyHarLog.getCreator());
        return newHar;
    }
}
