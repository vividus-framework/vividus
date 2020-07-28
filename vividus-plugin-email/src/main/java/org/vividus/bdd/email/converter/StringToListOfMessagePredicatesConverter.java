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

package org.vividus.bdd.email.converter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang3.function.FailablePredicate;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractChainableParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.vividus.bdd.email.factory.EmailParameterFilterFactory;

@Named
public class StringToListOfMessagePredicatesConverter
        extends AbstractChainableParameterConverter<ExamplesTable, List<FailablePredicate<Message, MessagingException>>>
{
    @Override
    public List<FailablePredicate<Message, MessagingException>> convertValue(ExamplesTable table, Type type)
    {
        return table.getRowsAsParameters().stream()
                                          .map(asPredicate())
                                          .collect(Collectors.toList());
    }

    private static Function<Parameters, FailablePredicate<Message, MessagingException>> asPredicate()
    {
        return p ->
        {
            String ruleKey = p.valueAs("rule", String.class);
            EmailParameterFilterFactory factory = p.valueAs("parameter", EmailParameterFilterFactory.class);
            String value = p.valueAs("value", String.class);
            return factory.createFilter(ruleKey, value);
        };
    }
}
