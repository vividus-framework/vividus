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

package org.vividus.bdd.steps;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.expression.IExpressionProcessor;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.freemarker.FreemarkerProcessor;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@ExtendWith(MockitoExtension.class)
class FreemarkerStepsTests
{
    private static final String VARIABLE_NAME = "name";
    private static final String TEMPLATE_PATH = "org/vividus/bdd/steps/";
    private static final String TEMPLATE_FILE_NAME = "simple.ftl";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String CONTEX_KEY = "context-key";
    private static final String CONTEX_VALUE = "context-value";
    private static final String TABLE_KEY = "table-key";
    private static final String TABLE_VALUE = "table-value";
    private static final String PARAMETERS = "parameters";
    private static final ExamplesTable TABLE = new ExamplesTable(String.format("|%s|%n|%s|", TABLE_KEY, TABLE_VALUE));

    @Mock private IBddVariableContext bddVariableContext;

    static Stream<Arguments> dataProvider()
    {
        return Stream.of(
            arguments(new ExamplesTable("|param|\n|coming|"), TEMPLATE_FILE_NAME),
            arguments(new ExamplesTable("|param|parameters|\n|coming|1|"), "parameters.ftl")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void shouldInitVariableUsingTemplate(ExamplesTable table, String template) throws IOException, TemplateException
    {
        ExpressionAdaptor adaptor = new ExpressionAdaptor();
        adaptor.setProcessors(List.of(new TestExpressionProcessor()));
        FreemarkerSteps steps = new FreemarkerSteps(true, createConfiguration(), bddVariableContext, adaptor);
        steps.initVariableUsingTemplate(SCOPES, VARIABLE_NAME, TEMPLATE_PATH + template, table);
        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, "Winter is coming\n");
    }

    @Test
    void shouldResolveBddVariables() throws IOException, TemplateException
    {
        try (MockedConstruction<FreemarkerProcessor> mockedProcessor = mockConstruction(FreemarkerProcessor.class))
        {
            when(bddVariableContext.getVariables()).thenReturn(Map.of(CONTEX_KEY, CONTEX_VALUE));

            FreemarkerSteps steps = new FreemarkerSteps(true, createConfiguration(), bddVariableContext,
                    new ExpressionAdaptor());

            steps.initVariableUsingTemplate(SCOPES, VARIABLE_NAME, TEMPLATE_FILE_NAME, TABLE);

            FreemarkerProcessor processor = mockedProcessor.constructed().get(0);
            Map<String, Object> parameters = Map.of(
                TABLE_KEY, List.of(TABLE_VALUE),
                CONTEX_KEY, CONTEX_VALUE,
                PARAMETERS, Map.of(CONTEX_KEY, CONTEX_VALUE, TABLE_KEY, List.of(TABLE_VALUE))
            );
            verify(processor).process(TEMPLATE_FILE_NAME, parameters, StandardCharsets.UTF_8);
        }
    }

    @Test
    void shouldNotResolveBddVariables() throws IOException, TemplateException
    {
        try (MockedConstruction<FreemarkerProcessor> mockedProcessor = mockConstruction(FreemarkerProcessor.class))
        {
            FreemarkerSteps steps = new FreemarkerSteps(false, createConfiguration(), bddVariableContext,
                    new ExpressionAdaptor());

            steps.initVariableUsingTemplate(SCOPES, VARIABLE_NAME, TEMPLATE_FILE_NAME, TABLE);

            FreemarkerProcessor processor = mockedProcessor.constructed().get(0);
            Map<String, Object> parameters = Map.of(
                TABLE_KEY, List.of(TABLE_VALUE),
                PARAMETERS, Map.of(TABLE_KEY, List.of(TABLE_VALUE))
            );
            verify(processor).process(TEMPLATE_FILE_NAME, parameters, StandardCharsets.UTF_8);
            verify(bddVariableContext, times(0)).getVariables();
        }
    }

    private Configuration createConfiguration()
    {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(getClass(), "/");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.toString());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return configuration;
    }

    private static final class TestExpressionProcessor implements IExpressionProcessor<String>
    {
        @Override
        public Optional<String> execute(String expression)
        {
            return Optional.of("Winter");
        }
    }
}
