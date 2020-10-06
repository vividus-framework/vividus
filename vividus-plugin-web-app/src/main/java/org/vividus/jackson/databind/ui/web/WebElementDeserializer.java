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

package org.vividus.jackson.databind.ui.web;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import org.openqa.selenium.WebElement;
import org.vividus.ui.action.SearchActions;
import org.vividus.ui.web.util.ElementUtil;

@Named
public class WebElementDeserializer extends JsonDeserializer<Supplier<?>> implements ContextualDeserializer
{
    @Inject private SearchActions searchActions;
    @Inject private ElementUtil elementUtil;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
    {
        JavaType requiredType = property.getType();
        return "Ljava/util/function/Supplier<Ljava/util/Optional<Lorg/openqa/selenium/WebElement;>;>;"
                .equals(requiredType.getGenericSignature()) ? this : null;
    }

    @Override
    public Supplier<Optional<WebElement>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        String locator = p.getText();
        return elementUtil.getElement(locator, searchActions);
    }
}
