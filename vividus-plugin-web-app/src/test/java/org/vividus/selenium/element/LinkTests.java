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

package org.vividus.selenium.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class LinkTests
{
    @Mock
    private WebElement wrappedElement;

    @InjectMocks
    private Link link;

    @Test
    void testGetWrappedElement()
    {
        assertEquals(wrappedElement, link.getWrappedElement());
    }

    @Test
    void testGetUrl()
    {
        String url = "https://www.vividus.org/";
        when(wrappedElement.getAttribute("href")).thenReturn(url);
        assertEquals(url, link.getUrl());
    }

    @Test
    void testGetTarget()
    {
        String target = "_blank";
        when(wrappedElement.getAttribute("target")).thenReturn(target);
        assertEquals(target, link.getTarget());
    }
}
