/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.accessibility.model.axe;

import java.util.List;

public class Target
{
    private final boolean insideShadowDom;
    private final List<String> selectorsChain;

    public Target(boolean insideShadowDom, List<String> selectorsChain)
    {
        this.insideShadowDom = insideShadowDom;
        this.selectorsChain = selectorsChain;
    }

    public boolean isInsideShadowDom()
    {
        return insideShadowDom;
    }

    public List<String> getSelectorsChain()
    {
        return selectorsChain;
    }
}
