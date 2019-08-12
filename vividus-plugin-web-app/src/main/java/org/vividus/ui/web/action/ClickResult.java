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

package org.vividus.ui.web.action;

public class ClickResult
{
    private boolean clicked;
    private boolean newPageLoaded;

    public boolean isClicked()
    {
        return clicked;
    }

    public void setClicked(boolean clicked)
    {
        this.clicked = clicked;
    }

    public boolean isNewPageLoaded()
    {
        return newPageLoaded;
    }

    public void setNewPageLoaded(boolean newPageLoaded)
    {
        this.newPageLoaded = newPageLoaded;
    }
}
