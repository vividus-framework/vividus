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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ResolvingSelfReferencesEagerlyTransformerTests
{
    private final ResolvingSelfReferencesEagerlyTransformer transformer = new ResolvingSelfReferencesEagerlyTransformer(
            new ParameterControls());
    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @ParameterizedTest
    @CsvSource({
            "'|A|B|C|\n|a|<A>|c|',                     '|A|B|C|\n|a|a|c|'",
            "'|A|B|C|\n|a1|<A>|c1|\n|a2|<A>|c2|',      '|A|B|C|\n|a1|a1|c1|\n|a2|a2|c2|'",
            "'|A|B|C|D|E|F|\n|<C>|<A>|c|<F>|<D>|<B>|', '|A|B|C|D|E|F|\n|c|c|c|c|c|c|'",
            "'|A|B|C|\n|a|<A><C>|c|',                  '|A|B|C|\n|a|ac|c|'",
            "'|A|B|C|\n|a<p>|<p><A>|c|',               '|A|B|C|\n|a<p>|<p>a<p>|c|'",
            "'|A|B|C|\n|a|<A>|',                       '|A|B|C|\n|a|a|'",
            "'|A|B|\n|a|<A>|c|',                       '|A|B|\n|a|a|'",
            "'|A|B|C|\n|a|<<A>>|c|',                   '|A|B|C|\n|a|<<A>>|c|'"
    })
    void shouldTransform(String beforeTransform, String expectedResult)
    {
        assertEquals(expectedResult, transform(beforeTransform));
    }

    @ParameterizedTest
    @CsvSource({
            "'|A|B|C|\n|<B>|<C>|<A>|', A -> B -> C -> A",
            "'|A|B|C|\n|<B>|<C>|<B>|', B -> C -> B"
    })
    void shouldFailWhenSelfReferenceIsDetected(String input, String chain)
    {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transform(input));
        assertEquals("Circular chain of references is found: " + chain, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "'|A|\n|<A>|'",
            "'|A|\n|a<A>b|'"
    })
    void shouldFailWhenChainOfReferencesIsDetected(String input)
    {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transform(input));
        assertEquals("Circular self reference is found in column 'A'", exception.getMessage());
    }

    private String transform(String beforeTransform)
    {
        return transformer.transform(beforeTransform, new TableParsers(parameterConverters),
                new TableProperties("", keywords, parameterConverters));
    }
}
