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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jbehave.core.annotations.Given;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.freemarker.FreemarkerProcessor;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class FreemarkerSteps
{
    private final IBddVariableContext bddVariableContext;
    private final FreemarkerProcessor freemarkerProcessor;

    private final boolean resolveBddVariables;

    public FreemarkerSteps(boolean resolveBddVariables, Configuration configuration,
            IBddVariableContext bddVariableContext, ExpressionAdaptor expressionAdaptor)
    {
        this.bddVariableContext = bddVariableContext;
        this.freemarkerProcessor = new FreemarkerProcessor(configuration);
        configuration.setSharedVariable("execVividusExpression",
                new FreemarkerVividusExpressionProcessor(expressionAdaptor));
        this.resolveBddVariables = resolveBddVariables;
    }

    /**
     * This step initializes BDD variable with a result of given template processing
     * It's allowed to use <i>global</i>, <i>next batches</i>, <i>scenario</i> and <i>story</i> variables within
     * templates by referring them using the variable reference notation. Note that the parameters passed to the
     * step take precedence over the variables.
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     * @param templatePath Freemarker template file path
     * @param templateParameters Parameters processed by template. Any valid ExamplesTable.
     * <p>For example, if template file is the following:</p>
     * {<br>
     *  "id": "12345",<br>
     *  "version": 1,<br>
     *  "dateTime": "${dateTime}",<br>
     *  "adherenceDateTime": "${adherenceDateTime}",<br>
     *  "didAdhere": true<br>
     * }
     * <p>Parameters required for Freemarker template in ExamplesTable will be the following:</p>
     * <table border="1" style="width:70%">
     * <caption>Table of parameters</caption>
     * <tr>
     * <td>dateTime</td>
     * <td>adherenceDateTime</td>
     * </tr>
     * <tr>
     * <td>2016-05-19T15:30:34</td>
     * <td>2016-05-19T14:43:12</td>
     * </tr>
     * </table>
     * @throws IOException in case of any error happened at I/O operations
     * @throws TemplateException in case of any error at template processing
     */
    @Given("I initialize the $scopes variable `$variableName` using template `$templatePath` with parameters:"
            + "$templateParameters")
    public void initVariableUsingTemplate(Set<VariableScope> scopes, String variableName, String templatePath,
            List<Map<String, String>> templateParameters) throws IOException, TemplateException
    {
        Map<String, Object> dataModel = new HashMap<>();
        if (resolveBddVariables)
        {
            dataModel.putAll(bddVariableContext.getVariables());
        }
        Map<String, List<String>> inputModel = templateParameters.stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(groupingBy(Entry::getKey, HashMap::new,
                        mapping(Entry::getValue, toCollection(ArrayList::new)))
                );

        dataModel.putAll(inputModel);

        Map<String, Object> parameters = new HashMap<>();
        String parametersKey = "parameters";
        parameters.put(parametersKey, dataModel);
        if (!dataModel.containsKey(parametersKey))
        {
            parameters.putAll(dataModel);
        }
        String value = freemarkerProcessor.process(templatePath, parameters, StandardCharsets.UTF_8);
        bddVariableContext.putVariable(scopes, variableName, value);
    }

    private static final class FreemarkerVividusExpressionProcessor implements TemplateMethodModelEx
    {
        private final ExpressionAdaptor expressionAdaptor;

        private FreemarkerVividusExpressionProcessor(ExpressionAdaptor expressionAdaptor)
        {
            this.expressionAdaptor = expressionAdaptor;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public Object exec(List arguments) throws TemplateModelException
        {
            String expressionName = arguments.get(0).toString();
            return arguments.subList(1, arguments.size())
                            .stream()
                            .map(Object::toString)
                            .collect(Collectors.collectingAndThen(Collectors.joining(",", expressionName + "(", ")"),
                                    expressionAdaptor::processExpression));
        }
    }
}
