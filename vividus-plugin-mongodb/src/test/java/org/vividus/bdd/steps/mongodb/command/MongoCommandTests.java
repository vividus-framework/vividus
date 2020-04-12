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

package org.vividus.bdd.steps.mongodb.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoCommandTests
{
    @Mock
    private Bson bson;
    @Mock
    private FindIterable<Document> findIterable;

    @SuppressWarnings("unchecked")
    @Test
    void testFind()
    {
        MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
        when(mongoCollection.find(bson)).thenReturn(findIterable);
        Object output = MongoCommand.FIND.apply(Function.identity(), bson).apply(mongoCollection);
        assertEquals(findIterable, output);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testProjection()
    {
        FindIterable<Document> projectionIterable = mock(FindIterable.class);
        when(findIterable.projection(bson)).thenReturn(projectionIterable);
        Object output = MongoCommand.PROJECTION.apply(Function.identity(), bson).apply(findIterable);
        assertEquals(projectionIterable, output);
    }

    @Test
    void testCollect()
    {
        Document document = mock(Document.class);
        when(findIterable.spliterator()).thenReturn(List.of(document).spliterator());
        Object output = MongoCommand.COLLECT.apply(Function.identity(), bson).apply(findIterable);
        assertEquals(List.of(document), output);
    }

    @Test
    void testCount()
    {
        Document document = mock(Document.class);
        when(findIterable.spliterator()).thenReturn(List.of(document).spliterator());
        Object output = MongoCommand.COUNT.apply(Function.identity(), bson).apply(findIterable);
        assertEquals(1L, output);
    }
}
