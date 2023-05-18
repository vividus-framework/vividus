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

package org.vividus.visual.eyes.model;

import java.util.Set;

import com.applitools.eyes.config.Configuration;

import org.vividus.ui.action.search.Locator;
import org.vividus.visual.model.AbstractVisualCheck;
import org.vividus.visual.model.VisualActionType;

public class ApplitoolsVisualCheck extends AbstractVisualCheck
{
    private String readApiKey;
    private String batchName;
    private Set<Locator> elementsToIgnore;
    private Set<Locator> areasToIgnore;
    private Configuration configuration;

    public ApplitoolsVisualCheck(String batchName, String baselineName, VisualActionType action)
    {
        super(baselineName, action);
        this.batchName = batchName;
    }

    public String getReadApiKey()
    {
        return readApiKey;
    }

    public void setReadApiKey(String readApiKey)
    {
        this.readApiKey = readApiKey;
    }

    public String getBatchName()
    {
        return batchName;
    }

    public void setElementsToIgnore(Set<Locator> elementsToIgnore)
    {
        this.elementsToIgnore = elementsToIgnore;
    }

    public void setAreasToIgnore(Set<Locator> areasToIgnore)
    {
        this.areasToIgnore = areasToIgnore;
    }

    public Set<Locator> getElementsToIgnore()
    {
        return elementsToIgnore;
    }

    public Set<Locator> getAreasToIgnore()
    {
        return areasToIgnore;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
