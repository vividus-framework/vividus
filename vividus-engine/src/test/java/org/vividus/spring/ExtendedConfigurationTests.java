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

package org.vividus.spring;

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
import org.jbehave.core.expressions.ExpressionResolver;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableParsers;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.IPathFinder;
import org.vividus.batch.BatchConfiguration;
import org.vividus.log.LoggingTableTransformerMonitor;
import org.vividus.steps.ParameterConvertersDecorator;
import org.vividus.steps.PlaceholderResolver;

@ExtendWith(MockitoExtension.class)
class ExtendedConfigurationTests
{
    private static final String SEPARATOR = "|";

    @Mock private IPathFinder pathFinder;
    @Mock private ExpressionResolver expressionResolver;
    @Mock private PlaceholderResolver placeholderResolver;
    @Mock private TableParsers tableParsers;

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
        var compositePathPatterns = "**/*.steps";
        var compositePaths = List.of("/path/to/composite.steps");
        when(pathFinder.findPaths(argPathResolution(compositePathPatterns))).thenReturn(compositePaths);

        var aliasPathPatterns = "**/*.json";
        var aliasPaths = List.of("/path/to/aliases.json");
        when(pathFinder.findPaths(argPathResolution(aliasPathPatterns))).thenReturn(aliasPaths);

        ExamplesTableFactory examplesTableFactory = mock();
        when(configuration.examplesTableFactory()).thenReturn(examplesTableFactory);

        Map<Class<?>, Object> constructedMocks = new HashMap<>();
        List<ParameterConverter<?, ?>> parameterConverterList = List.of();

        try (var ignoredKeywords = mockConstruction(
                Keywords.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(Keywords.defaultKeywords()), context.arguments());
                    constructedMocks.put(Keywords.class, mock);
                });
            var ignoredParser = mockConstruction(
                RegexStoryParser.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(examplesTableFactory), context.arguments());
                    constructedMocks.put(RegexStoryParser.class, mock);
                });
            var ignoredDecorator = mockConstruction(
                ParameterConvertersDecorator.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(configuration, placeholderResolver), context.arguments());
                    constructedMocks.put(ParameterConvertersDecorator.class, mock);

                    when(mock.addConverters(parameterConverterList)).thenReturn(mock);
                });
            var ignoredTransformerMonitor = mockConstruction(
                 LoggingTableTransformerMonitor.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(tableParsers), context.arguments());
                    constructedMocks.put(LoggingTableTransformerMonitor.class, mock);
                }))
        {
            StoryControls storyControls = mock();
            configuration.setCustomConverters(parameterConverterList);
            configuration.setCompositePaths(compositePathPatterns);
            configuration.setAliasPaths(aliasPathPatterns);
            configuration.setStoryControls(storyControls);
            StepMonitor stepMonitor = mock();
            configuration.setStepMonitors(List.of(stepMonitor));

            configuration.init();

            var ordered = inOrder(configuration);
            ordered.verify(configuration).useKeywords((Keywords) constructedMocks.get(Keywords.class));
            ordered.verify(configuration).useCompositePaths(new HashSet<>(compositePaths));
            ordered.verify(configuration).useAliasPaths(new HashSet<>(aliasPaths));
            ordered.verify(configuration).useExpressionResolver(expressionResolver);
            ordered.verify(configuration).useParameterConverters(
                    (ParameterConvertersDecorator) constructedMocks.get(ParameterConvertersDecorator.class));
            ordered.verify(configuration).useTableTransformerMonitor(
                    (LoggingTableTransformerMonitor) constructedMocks.get(LoggingTableTransformerMonitor.class));
            ordered.verify(configuration).useStoryParser(
                    (RegexStoryParser) constructedMocks.get(RegexStoryParser.class));
            ordered.verify(configuration).useStoryControls(storyControls);

            verifyStepMonitor(stepMonitor);
        }
    }

    private void verifyStepMonitor(StepMonitor expectedStepMonitorDelegate)
    {
        var actualStepMonitor = configuration.stepMonitor();
        assertThat(actualStepMonitor, instanceOf(DelegatingStepMonitor.class));
        var step = "step";
        var dryRun = true;
        Method method = null;
        actualStepMonitor.beforePerforming(step, dryRun, method);
        verify(expectedStepMonitorDelegate).beforePerforming(step, dryRun, method);
    }

    @Test
    void testSetCustomTableTransformers() throws IOException
    {
        var name = "customTableTransformer";
        var tableAsString = "tableAsString";
        var tableProperties = new TableProperties("", new Keywords(), new ParameterConverters());
        TableTransformer tableTransformer = mock();
        var transformed = "transformed";
        when(tableTransformer.transform(tableAsString, tableParsers, tableProperties)).thenReturn(transformed);
        configuration.setCustomTableTransformers(Map.of(name, tableTransformer));
        configuration.init();
        var result = configuration.tableTransformers().transform(name, tableAsString, tableParsers, tableProperties);
        assertEquals(transformed, result);
    }

    private static BatchConfiguration argPathResolution(String compositePathPatterns)
    {
        var resourceIncludePatterns = List.of(compositePathPatterns);
        return argThat(batch -> batch != null && "/".equals(batch.getResourceLocation())
                && resourceIncludePatterns.equals(batch.getResourceIncludePatterns())
                && List.of().equals(batch.getResourceExcludePatterns()));
    }

    @Test
    void testSetDryRun() throws IOException
    {
        var storyControls = new StoryControls();
        storyControls.doDryRun(true);
        configuration.setStoryControls(storyControls);
        configuration.init();
        assertTrue(configuration.storyControls().dryRun());
    }

    @Test
    void shouldSetViewGenerator()
    {
        ViewGenerator viewGenerator = mock();
        configuration.setViewGenerator(Optional.of(viewGenerator));
        assertEquals(viewGenerator, configuration.viewGenerator());
    }

    @Test
    void shouldNotSetViewGeneratorIfEmptyOptionalUsed()
    {
        configuration.setViewGenerator(Optional.empty());
        verify(configuration, never()).useViewGenerator(any());
    }

    @Test
    void shouldSetStoryExecutionComparator()
    {
        Comparator<Story> storyExecutionComparator = mock();
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
