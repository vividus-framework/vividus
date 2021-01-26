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

package org.vividus.bdd.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;

@ExtendWith(MockitoExtension.class)
class GroovyExpressionProcessorTests
{
    @Mock
    private GroovyScriptEvaluator evaulator;

    @InjectMocks
    private GroovyExpressionProcessor processor;

    @Test
    void shouldResolveGroovyExpressions()
    {
        String script = "return 3 + 2";
        when(evaulator.evaluate(argThat(s -> {
            try
            {
                return script.equals(s.getScriptAsString());
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }))).thenReturn(5);
        assertEquals("5", processor.execute("evalGroovy(return 3 + 2)").get());
    }
}
