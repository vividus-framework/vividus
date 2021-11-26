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

package org.vividus.email.converter;

import java.lang.reflect.Type;

import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang3.function.FailablePredicate;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.vividus.email.factory.EmailParameterFilterFactory;

@Named
public class ParametersToMessagePredicateConverter
        extends AbstractParameterConverter<Parameters, FailablePredicate<Message, MessagingException>>
{
    @Override
    public FailablePredicate<Message, MessagingException> convertValue(Parameters parameters, Type type)
    {
        String ruleKey = parameters.valueAs("rule", String.class);
        EmailParameterFilterFactory factory = parameters.valueAs("parameter", EmailParameterFilterFactory.class);
        String value = parameters.valueAs("value", String.class);
        return factory.createFilter(ruleKey, value);
    }
}
