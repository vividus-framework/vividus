/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.validation;

import java.util.function.Consumer;

import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.web.ViewportPresence;
import org.vividus.ui.web.action.ScrollActions;

public class ScrollValidations<T>
{
    private final ScrollActions<T> scrollActions;
    private final ISoftAssert softAssert;

    public ScrollValidations(ScrollActions<T> scrollActions, ISoftAssert softAssert)
    {
        this.scrollActions = scrollActions;
        this.softAssert = softAssert;
    }

    public void assertElementPositionAgainstViewport(T element, ViewportPresence presence)
    {
        boolean inViewport = scrollActions.isElementInViewport(element);
        String message = inViewport ? "Element is in viewport" : "Element is not in viewport";

        Consumer<String> messageConsumer;
        if (presence == ViewportPresence.IS == inViewport)
        {
            messageConsumer = softAssert::recordPassedAssertion;
        }
        else
        {
            messageConsumer = softAssert::recordFailedAssertion;
        }
        messageConsumer.accept(message);
    }
}
