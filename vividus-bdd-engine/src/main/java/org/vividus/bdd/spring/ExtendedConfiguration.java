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

package org.vividus.bdd.spring;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.DelegatingStepMonitor;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters.ChainableParameterConverter;
import org.jbehave.core.steps.StepMonitor;
import org.vividus.bdd.IPathFinder;
import org.vividus.bdd.batch.BatchResourceConfiguration;
import org.vividus.bdd.steps.ExpressionAdaptor;
import org.vividus.bdd.steps.ParameterConvertersDecorator;
import org.vividus.bdd.steps.VariableResolver;

public class ExtendedConfiguration extends Configuration
{
    private IPathFinder pathFinder;
    private VariableResolver variableResolver;
    private ExpressionAdaptor expressionAdaptor;
    private List<ChainableParameterConverter<?, ?>> customConverters;
    private List<StepMonitor> stepMonitors;
    private Map<String, TableTransformer> customTableTransformers;
    private String compositePaths;
    private StoryControls storyControls;
    private String examplesTableHeaderSeparator;
    private String examplesTableValueSeparator;
    private ParameterControls parameterControls;

    public void init() throws IOException
    {
        initKeywords();
        initCompositePaths();
        useParameterControls(parameterControls);
        useParameterConverters(new ParameterConvertersDecorator(this, variableResolver, expressionAdaptor)
                .addConverters(customConverters));
        useStoryParser(new RegexStoryParser(keywords(), examplesTableFactory()));
        TableTransformers transformers = tableTransformers();
        customTableTransformers.forEach(transformers::useTransformer);
        useStepMonitor(new DelegatingStepMonitor(stepMonitors));
        useStoryControls(storyControls);
    }

    private void initKeywords()
    {
        Map<String, String> keywords = Keywords.defaultKeywords();
        keywords.put(Keywords.EXAMPLES_TABLE_HEADER_SEPARATOR, examplesTableHeaderSeparator);
        keywords.put(Keywords.EXAMPLES_TABLE_VALUE_SEPARATOR, examplesTableValueSeparator);
        useKeywords(new Keywords(keywords));
    }

    private void initCompositePaths() throws IOException
    {
        BatchResourceConfiguration compositeStepsBatch = new BatchResourceConfiguration();
        compositeStepsBatch.setResourceLocation("/");
        compositeStepsBatch.setResourceIncludePatterns(compositePaths);
        compositeStepsBatch.setResourceExcludePatterns(null);
        useCompositePaths(new HashSet<>(pathFinder.findPaths(compositeStepsBatch)));
    }

    public void setPathFinder(IPathFinder pathFinder)
    {
        this.pathFinder = pathFinder;
    }

    public void setStoryLoader(StoryLoader storyLoader)
    {
        useStoryLoader(storyLoader);
    }

    public void setStoryReporterBuilder(StoryReporterBuilder storyReporterBuilder)
    {
        useStoryReporterBuilder(storyReporterBuilder);
    }

    @Inject
    public void setViewGenerator(Optional<ViewGenerator> viewGenerator)
    {
        viewGenerator.ifPresent(this::useViewGenerator);
    }

    @Inject
    public void setStoryExecutionComparator(Optional<Comparator<Story>> storyExecutionComparator)
    {
        storyExecutionComparator.ifPresent(this::useStoryExecutionComparator);
    }

    @Inject
    public void setStepMonitors(List<StepMonitor> stepMonitors)
    {
        this.stepMonitors = Collections.unmodifiableList(stepMonitors);
    }

    public void setCompositePaths(String compositePaths)
    {
        this.compositePaths = compositePaths;
    }

    public void setVariableResolver(VariableResolver variableResolver)
    {
        this.variableResolver = variableResolver;
    }

    @Inject
    public void setCustomConverters(List<ChainableParameterConverter<?, ?>> customConverters)
    {
        this.customConverters = Collections.unmodifiableList(customConverters);
    }

    @Inject
    public void setCustomTableTransformers(Map<String, TableTransformer> customTableTransformers)
    {
        this.customTableTransformers = customTableTransformers;
    }

    public void setExpressionAdaptor(ExpressionAdaptor expressionAdaptor)
    {
        this.expressionAdaptor = expressionAdaptor;
    }

    public void setStoryControls(StoryControls storyControls)
    {
        this.storyControls = storyControls;
    }

    public void setExamplesTableHeaderSeparator(String examplesTableHeaderSeparator)
    {
        this.examplesTableHeaderSeparator = examplesTableHeaderSeparator;
    }

    public void setExamplesTableValueSeparator(String examplesTableValueSeparator)
    {
        this.examplesTableValueSeparator = examplesTableValueSeparator;
    }

    public void setParameterControls(ParameterControls parameterControls)
    {
        this.parameterControls = parameterControls;
    }
}
