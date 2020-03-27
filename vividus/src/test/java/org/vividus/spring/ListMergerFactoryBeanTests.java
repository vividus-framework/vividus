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

package org.vividus.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ListMergerFactoryBeanTests
{
    private ListMergerFactoryBean<String> bean = new ListMergerFactoryBean<>();

    @Test
    void shouldMergeLists()
    {
        String str1 = "str1";
        String str2 = "str2";
        String sttr3 = "sttr3";
        String str4 = "str4";
        bean.setLists(List.of(List.of(str1, str2), List.of(sttr3, str4)));
        assertEquals(List.of(str1, str2, sttr3, str4), bean.getObject());
    }

    @Test
    void shouldBeSingleton()
    {
        assertTrue(bean.isSingleton());
    }

    @Test
    void shouldContainObjectOfListType()
    {
        assertEquals(List.class, bean.getObjectType());
    }
}
