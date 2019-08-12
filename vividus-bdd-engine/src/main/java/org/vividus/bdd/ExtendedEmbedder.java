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

package org.vividus.bdd;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.steps.InjectableStepsFactory;

public class ExtendedEmbedder extends Embedder
{
    private IEmbedderControlsProvider embedderControlsProvider;

    @Override
    public EmbedderControls embedderControls()
    {
        return embedderControlsProvider.getDefault();
    }

    protected IEmbedderControlsProvider getEmbedderControlsProvider()
    {
        return embedderControlsProvider;
    }

    public void setEmbedderControlsProvider(IEmbedderControlsProvider embedderControlsProvider)
    {
        this.embedderControlsProvider = embedderControlsProvider;
    }

    public void setConfiguration(Configuration configuration)
    {
        useConfiguration(configuration);
    }

    public void setMetaFilters(List<String> metaFilters)
    {
        useMetaFilters(metaFilters);
    }

    public void setStepFactory(InjectableStepsFactory stepsFactory)
    {
        useStepsFactory(stepsFactory);
    }

    public void setEmbedderMonitor(EmbedderMonitor embedderMonitor)
    {
        useEmbedderMonitor(embedderMonitor);
    }
}
