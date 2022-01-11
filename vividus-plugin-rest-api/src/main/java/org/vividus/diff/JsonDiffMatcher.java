/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.diff;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.vividus.reporter.event.IAttachmentPublisher;

import io.qameta.allure.jsonunit.AbstractJsonPatchMatcher;
import io.qameta.allure.jsonunit.JsonPatchListener;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;

public class JsonDiffMatcher extends AbstractJsonPatchMatcher<JsonDiffMatcher> implements Matcher<String>
{
    private final IAttachmentPublisher attachmentPublisher;
    private final String expected;
    private final Map<Object, Boolean> matchCache = new HashMap<>();

    public JsonDiffMatcher(IAttachmentPublisher attachmentPublisher, String expected)
    {
        this.attachmentPublisher = attachmentPublisher;
        this.expected = expected;
        withDifferenceListener(new JsonPatchListener());
    }

    @Override
    protected void render(DifferenceListener listener)
    {
        JsonPatchListener jsonDiffListener = (JsonPatchListener) listener;
        Map<String, Object> model = Map.of("data", jsonDiffListener.getDiffModel());
        attachmentPublisher.publishAttachment("tpl/diff.ftl", model, "JSON Diff");
    }

    @Override
    public boolean matches(Object actual)
    {
        return matchCache.computeIfAbsent(actual, k -> super.matches(expected, k));
    }

    @Override
    public void describeMismatch(Object item, Description mismatchDescription)
    {
        String differences = super.getDifferences();
        mismatchDescription.appendText(null != differences ? differences : item.toString());
    }

    @Override
    public void _dont_implement_Matcher___instead_extend_BaseMatcher_()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(String.valueOf(expected));
    }
}
