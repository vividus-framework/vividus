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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.util.function.CheckedPredicate;

class StringToListOfMessagePredicatesConverterTests
{
    @Test
    void testConvertValue() throws MessagingException
    {
        String text = "Text";
        Message message = mock(Message.class);
        when(message.getSubject()).thenReturn(text);
        ExamplesTable table = new ExamplesTable("|parameter|rule|value|\n|SUBJECT|EQUAL_TO|Text|");

        StringToListOfMessagePredicatesConverter converter = new StringToListOfMessagePredicatesConverter();
        List<CheckedPredicate<Message, MessagingException>> filters = converter.convertValue(table, null);

        assertThat(filters, hasSize(1));
        CheckedPredicate<Message, MessagingException> filter = filters.get(0);
        Assertions.assertTrue(filter.test(message));
    }
}
