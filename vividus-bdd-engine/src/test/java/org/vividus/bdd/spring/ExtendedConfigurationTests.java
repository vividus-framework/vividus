/*
 * Copyright 2019-2021 the original author or authors.
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
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.DelegatingStepMonitor;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.StepMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.IPathFinder;
import org.vividus.bdd.batch.BatchResourceConfiguration;
import org.vividus.bdd.steps.ParameterConvertersDecorator;
import org.vividus.bdd.steps.PlaceholderResolver;

@ExtendWith(MockitoExtension.class)
class ExtendedConfigurationTests
{
    private static final String SEPARATOR = "|";

    @Mock private IPathFinder pathFinder;
    @Mock private PlaceholderResolver placeholderResolver;

    @InjectMocks
    @Spy
    private ExtendedConfiguration configuration;

    @BeforeEach
    void beforeEach()
    {
        configuration.setCustomConverters(List.of());
        configuration.setCustomTableTransformers(Map.of());
        configuration.setExamplesTableHeaderSeparator(SEPARATOR);
        configuration.setExamplesTableValueSeparator(SEPARATOR);
    }

    @SuppressWarnings("try")
    @Test
    void testInit() throws IOException
    {
        String compositePathPatterns = "**/*.steps";
        List<String> compositePaths = List.of("/path/to/composite.steps");
        when(pathFinder.findPaths(equalToCompositeStepsBatch(compositePathPatterns))).thenReturn(compositePaths);

        ExamplesTableFactory examplesTableFactory = mock(ExamplesTableFactory.class);
        when(configuration.examplesTableFactory()).thenReturn(examplesTableFactory);

        Map<Class<?>, Object> constructedMocks = new HashMap<>();
        List<ParameterConverter<?, ?>> parameterConverterList = List.of();

        try (MockedConstruction<Keywords> ignoredKeywords = mockConstruction(
                Keywords.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(Keywords.defaultKeywords()), context.arguments());
                    constructedMocks.put(Keywords.class, mock);
                });
            MockedConstruction<RegexStoryParser> ignoredParser = mockConstruction(
                RegexStoryParser.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(examplesTableFactory), context.arguments());
                    constructedMocks.put(RegexStoryParser.class, mock);
                });
            MockedConstruction<ParameterConvertersDecorator> ignoredDecorator = mockConstruction(
                ParameterConvertersDecorator.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(configuration, placeholderResolver), context.arguments());
                    constructedMocks.put(ParameterConvertersDecorator.class, mock);

                    when(mock.addConverters(parameterConverterList)).thenReturn(mock);
                }))
        {
            StoryControls storyControls = mock(StoryControls.class);
            configuration.setCustomConverters(parameterConverterList);
            configuration.setCompositePaths(compositePathPatterns);
            configuration.setStoryControls(storyControls);
            StepMonitor stepMonitor = mock(StepMonitor.class);
            configuration.setStepMonitors(List.of(stepMonitor));

            configuration.init();

            InOrder ordered = inOrder(configuration);
            ordered.verify(configuration).useKeywords((Keywords) constructedMocks.get(Keywords.class));
            ordered.verify(configuration).useCompositePaths(new HashSet<>(compositePaths));
            ordered.verify(configuration).useParameterConverters(
                    (ParameterConvertersDecorator) constructedMocks.get(ParameterConvertersDecorator.class));
            ordered.verify(configuration).useStoryParser(
                    (RegexStoryParser) constructedMocks.get(RegexStoryParser.class));
            ordered.verify(configuration).useStoryControls(storyControls);

            verifyStepMonitor(stepMonitor);
        }
    }

    private void verifyStepMonitor(StepMonitor expectedStepMonitorDelegate)
    {
        StepMonitor actualStepMonitor = configuration.stepMonitor();
        assertThat(actualStepMonitor, instanceOf(DelegatingStepMonitor.class));
        String step = "step";
        boolean dryRun = true;
        Method method = null;
        actualStepMonitor.beforePerforming(step, dryRun, method);
        verify(expectedStepMonitorDelegate).beforePerforming(step, dryRun, method);
    }

    @Test
    void testSetCustomTableTransformers() throws IOException
    {
        String name = "customTableTransformer";
        TableTransformer tableTransformer = mock(TableTransformer.class);
        configuration.setCustomTableTransformers(Map.of(name, tableTransformer));
        configuration.init();
        String tableAsString = "tableAsString";
        TableProperties tableProperties = new TableProperties("", new Keywords(), new ParameterConverters());
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
    void testSetDryRun() throws IOException
    {
        StoryControls storyControls = new StoryControls();
        storyControls.doDryRun(true);
        configuration.setStoryControls(storyControls);
        configuration.init();
        assertTrue(configuration.storyControls().dryRun());
    }

    @Test
    void shouldSetViewGenerator()
    {
        ViewGenerator viewGenerator = mock(ViewGenerator.class);
        configuration.setViewGenerator(Optional.of(viewGenerator));
        assertEquals(viewGenerator, configuration.viewGenerator());
    }

    @Test
    void shouldNotSetViewGeneratorIfEmptyOptionalUsed()
    {
        configuration.setViewGenerator(Optional.empty());
        verify(configuration, never()).useViewGenerator(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSetStoryExecutionComparator()
    {
        Comparator<Story> storyExecutionComparator = mock(Comparator.class);
        configuration.setStoryExecutionComparator(Optional.of(storyExecutionComparator));
        assertEquals(storyExecutionComparator, configuration.storyExecutionComparator());
    }

    @Test
    void shouldNotSetStoryExecutionComparatorIfEmptyOptionalUsed()
    {
        configuration.setStoryExecutionComparator(Optional.empty());
        verify(configuration, never()).useStoryExecutionComparator(any());
    }
}
