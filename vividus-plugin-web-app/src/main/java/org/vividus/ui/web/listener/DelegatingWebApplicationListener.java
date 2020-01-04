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

package org.vividus.ui.web.listener;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.vividus.ui.web.action.INavigateActions;

public class DelegatingWebApplicationListener implements IWebApplicationListener
{
    @Inject private INavigateActions navigateActions;
    private List<IWebApplicationListener> webApplicationListeners;

    @Override
    public void onLoad()
    {
        if (!webApplicationListeners.isEmpty())
        {
            webApplicationListeners.forEach(IWebApplicationListener::onLoad);
            navigateActions.refresh();
        }
    }

    public void setWebApplicationListeners(List<IWebApplicationListener> webApplicationListeners)
    {
        this.webApplicationListeners = Collections.unmodifiableList(webApplicationListeners);
    }
}
