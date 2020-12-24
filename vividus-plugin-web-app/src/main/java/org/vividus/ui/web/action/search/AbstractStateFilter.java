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

package org.vividus.ui.web.action.search;

import java.lang.reflect.ParameterizedType;

import javax.inject.Inject;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.IState;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.LocatorType;

public abstract class AbstractStateFilter<T extends Enum<T> & IState> implements IElementFilterAction
{
    private final Class<T> enumType;
    private final LocatorType locatorType;
    @Inject private IWebDriverProvider webDriverProvider;

    @SuppressWarnings("unchecked")
    public AbstractStateFilter(LocatorType locatorType)
    {
        this.locatorType = locatorType;
        this.enumType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public boolean matches(WebElement element, String stateName)
    {
        T state = Enum.valueOf(enumType, stateName);
        return matches(state.getExpectedCondition(element));
    }

    private boolean matches(ExpectedCondition<?> expectedCondition)
    {
        Object result = expectedCondition.apply(webDriverProvider.get());
        return result instanceof Boolean ? (boolean) result : result != null;
    }

    @Override
    public LocatorType getType()
    {
        return locatorType;
    }
}
