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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.DelegatingStepMonitor;
import org.jbehave.core.steps.ParameterConverters.ChainableParameterConverter;
import org.jbehave.core.steps.StepMonitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.bdd.IPathFinder;
import org.vividus.bdd.batch.BatchResourceConfiguration;
import org.vividus.bdd.steps.ExpressionAdaptor;
import org.vividus.bdd.steps.ParameterAdaptor;
import org.vividus.bdd.steps.ParameterConvertersDecorator;

@RunWith(PowerMockRunner.class)
public class ExtendedConfigurationTests
{
    private static final String SEPARATOR = "|";

    @Mock
    private IPathFinder pathFinder;

    @Mock
    private ParameterAdaptor parameterAdaptor;

    @Mock
    private ExpressionAdaptor expressionAdaptor;

    @InjectMocks
    private ExtendedConfiguration configuration;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        configuration.setCustomConverters(List.of());
        configuration.setCustomTableTransformers(Map.of());
        configuration.setExamplesTableHeaderSeparator(SEPARATOR);
        configuration.setExamplesTableValueSeparator(SEPARATOR);
    }

    @Test
    @PrepareForTest(ExtendedConfiguration.class)
    public void testInit() throws Exception
    {
        String compositePathPatterns = "**/*.steps";
        List<String> compositePaths = List.of("/path/to/composite.steps");
        when(pathFinder.findPaths(equalToCompositeStepsBatch(compositePathPatterns))).thenReturn(compositePaths);

        ExtendedConfiguration spy = spy(configuration);
        Keywords keywords = mock(Keywords.class);
        PowerMockito.whenNew(Keywords.class).withArguments(Keywords.defaultKeywords()).thenReturn(keywords);
        ExamplesTableFactory examplesTableFactory = mock(ExamplesTableFactory.class);
        when(spy.examplesTableFactory()).thenReturn(examplesTableFactory);
        RegexStoryParser regexStoryParser = mock(RegexStoryParser.class);
        PowerMockito.whenNew(RegexStoryParser.class).withArguments(keywords, examplesTableFactory)
                .thenReturn(regexStoryParser);
        ParameterConvertersDecorator parameterConverters = mock(ParameterConvertersDecorator.class);
        PowerMockito.whenNew(ParameterConvertersDecorator.class).withArguments(spy, parameterAdaptor, expressionAdaptor)
                .thenReturn(parameterConverters);
        List<ChainableParameterConverter<?, ?>> parameterConverterList = List.of();
        when(parameterConverters.addConverters(parameterConverterList)).thenReturn(parameterConverters);
        StoryControls storyControls = mock(StoryControls.class);
        spy.setCustomConverters(parameterConverterList);
        spy.setCompositePaths(compositePathPatterns);
        spy.setStoryControls(storyControls);
        List<StepMonitor> stepMonitors = List.of(mock(StepMonitor.class));
        spy.setStepMonitors(stepMonitors);
        spy.init();

        verify(spy).useKeywords(keywords);
        verify(spy).useCompositePaths(new HashSet<>(compositePaths));

        InOrder inOrder = inOrder(spy);
        inOrder.verify(spy).useParameterConverters(parameterConverters);
        inOrder.verify(spy).useStoryParser(regexStoryParser);

        verify(spy).useStoryControls(storyControls);
        verifyStepMonitor(spy, stepMonitors.get(0));
    }

    private static void verifyStepMonitor(ExtendedConfiguration spy, StepMonitor expectedStepMonitorDelegate)
    {
        StepMonitor actualStepMonitor = spy.stepMonitor();
        assertThat(actualStepMonitor, instanceOf(DelegatingStepMonitor.class));
        String step = "step";
        boolean dryRun = true;
        Method method = null;
        actualStepMonitor.beforePerforming(step, dryRun, method);
        verify(expectedStepMonitorDelegate).beforePerforming(step, dryRun, method);
    }

    @Test
    public void testSetCustomTableTransformers() throws IOException
    {
        String name = "customTableTransformer";
        TableTransformer tableTransformer = mock(TableTransformer.class);
        configuration.setCustomTableTransformers(Map.of(name, tableTransformer));
        configuration.init();
        String tableAsString = "tableAsString";
        TableProperties tableProperties = new TableProperties(new Properties());
        configuration.tableTransformers().transform(name, tableAsString, null, tableProperties);
        verify(tableTransformer).transform(tableAsString, null, tableProperties);
    }

    private static BatchResourceConfiguration equalToCompositeStepsBatch(String compositePathPatterns)
    {
        List<String> resourceIncludePatterns = List.of(compositePathPatterns);
        return argThat(batch -> "/".equals(batch.getResourceLocation())
                && resourceIncludePatterns.equals(batch.getResourceIncludePatterns())
                && List.of().equals(batch.getResourceExcludePatterns()));
    }

    @Test
    public void testSetDryRun() throws IOException
    {
        StoryControls storyControls = new StoryControls();
        storyControls.doDryRun(true);
        configuration.setStoryControls(storyControls);
        configuration.init();
        assertTrue(configuration.storyControls().dryRun());
    }

    @Test
    public void testSetViewGenerator()
    {
        ViewGenerator viewGenerator = mock(ViewGenerator.class);
        configuration.setViewGenerator(Optional.of(viewGenerator));
        assertEquals(viewGenerator, configuration.viewGenerator());
    }

    @Test
    public void shouldNotSetViewGeneratorIfEmptyOptionalUsed()
    {
        ExtendedConfiguration spy = Mockito.spy(configuration);
        configuration.setViewGenerator(Optional.empty());
        verify(spy, never()).useViewGenerator(any());
    }
}
