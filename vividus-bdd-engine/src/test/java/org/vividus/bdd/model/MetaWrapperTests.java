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

package org.vividus.bdd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.Test;

class MetaWrapperTests
{
    @Test
    void testGetOptionalPropertyValue()
    {
        Meta meta = mock(Meta.class);
        String propertyName = "name";
        String propertyValue = "value";
        when(meta.getProperty(propertyName)).thenReturn(propertyValue);
        MetaWrapper metaWrapper = new MetaWrapper(meta);
        assertEquals(Optional.of(propertyValue), metaWrapper.getOptionalPropertyValue(propertyName));
    }
}
