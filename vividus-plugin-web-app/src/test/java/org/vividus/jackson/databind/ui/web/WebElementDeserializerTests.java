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

package org.vividus.jackson.databind.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.SearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;

@ExtendWith(MockitoExtension.class)
class WebElementDeserializerTests
{
    @Mock
    private SearchActions searchActions;

    @InjectMocks
    private WebElementDeserializer deserializer;

    @Test
    void shouldReturnSupplierForWebElement() throws IOException
    {
        Optional<WebElement> expected = Optional.of(mock(WebElement.class));
        when(searchActions.findElement(new SearchAttributes(ActionAttributeType.ID, "id"))).thenReturn(expected);
        JsonParser parser = mock(JsonParser.class);
        when(parser.getText()).thenReturn("By.id(id)");
        assertEquals(expected, deserializer.deserialize(parser, null).get());
    }

    @Test
    void shouldReturnTypeBasedDeserializer()
    {
        shouldReturnTypeBasedDeserializer(
                "Ljava/util/function/Supplier<Ljava/util/Optional<Lorg/openqa/selenium/WebElement;>;>;", deserializer);
    }

    @Test
    void shouldReturnNullWhenNoTypeBasedDeserializerIsFound()
    {
        shouldReturnTypeBasedDeserializer(
                "Ljava/util/function/Supplier<Ljava/util/Optional<Ljava/lang/Stringt;>;>;", null);
    }

    void shouldReturnTypeBasedDeserializer(String type, JsonDeserializer<?> expected)
    {
        BeanProperty property = mock(BeanProperty.class);
        JavaType javaType = mock(JavaType.class);
        when(property.getType()).thenReturn(javaType);
        when(javaType.getGenericSignature()).thenReturn(type);
        assertEquals(expected, deserializer.createContextual(null, property));
    }
}
