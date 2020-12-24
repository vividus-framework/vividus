/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.diff;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Objects;

import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.IAttachmentPublisher;

import io.qameta.allure.jsonunit.DiffModel;
import io.qameta.allure.jsonunit.JsonPatchListener;

@ExtendWith(MockitoExtension.class)
class JsonDiffMatcherTests
{
    private static final String EXPECTED_DATA = "expected";
    private static final String ACTUAL_DATA = "actual";
    private static final String JSON = "{\"version\": 1}";

    @Mock
    private IAttachmentPublisher attachmentPublisher;

    @Test
    void testRender()
    {
        String patch = "patch";
        DiffModel diffModel = new DiffModel(ACTUAL_DATA, EXPECTED_DATA, patch);
        JsonPatchListener listener = mock(JsonPatchListener.class);
        when(listener.getDiffModel()).thenReturn(diffModel);
        JsonDiffMatcher matcher = spy(createMatcher(EXPECTED_DATA));
        matcher.render(listener);
        verify(listener).getDiffModel();
        verify(matcher).render(listener);
        verify(attachmentPublisher).publishAttachment(eq("tpl/diff.ftl"), argThat(e ->
        {
            @SuppressWarnings("unchecked")
            DiffModel diff = ((Map<String, DiffModel>) e).get("data");
            return Objects.equals(diff.getActual(), ACTUAL_DATA)
                    && Objects.equals(diff.getExpected(), EXPECTED_DATA)
                    && Objects.equals(diff.getPatch(), patch);
        }), eq("JSON Diff"));
        verifyNoMoreInteractions(listener, attachmentPublisher, matcher);
    }

    @Test
    void testMatches()
    {
        assertTrue(createMatcher(JSON).matches(JSON));
    }

    @Test
    void testDescribeMismatch()
    {
        Description description = mock(Description.class);
        Object object = mock(Object.class);
        JsonDiffMatcher matcher = createMatcher(EXPECTED_DATA);
        matcher.matches(JSON);
        matcher.describeMismatch(object, description);
        verify(description).appendText("JSON documents are different:\nDifferent value found in node \"\", expected:"
                + " <\"expected\"> but was: <{\"version\":1}>.\n");
        verifyNoMoreInteractions(description, object);
    }

    @Test
    void testDescribeMismatchItemMatches()
    {
        Description description = mock(Description.class);
        Object object = JSON;
        JsonDiffMatcher matcher = createMatcher(JSON);
        matcher.matches(JSON);
        matcher.describeMismatch(object, description);
        verify(description).appendText(JSON);
        verifyNoMoreInteractions(description);
    }

    @Test
    void testUnsupportedDeprecatedOperation()
    {
        assertThrows(UnsupportedOperationException.class,
            () -> createMatcher(EXPECTED_DATA)._dont_implement_Matcher___instead_extend_BaseMatcher_());
    }

    @Test
    void testDescribeTo()
    {
        Description description = mock(Description.class);
        JsonDiffMatcher matcher = spy(createMatcher(EXPECTED_DATA));
        matcher.describeTo(description);
        verify(description).appendText(EXPECTED_DATA);
        verify(matcher).describeTo(description);
        verifyNoMoreInteractions(description, matcher);
    }

    private JsonDiffMatcher createMatcher(String expected)
    {
        return new JsonDiffMatcher(attachmentPublisher, expected);
    }
}
