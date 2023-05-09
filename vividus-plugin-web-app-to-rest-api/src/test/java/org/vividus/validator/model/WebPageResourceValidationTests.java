/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.validator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

class WebPageResourceValidationTests
{
    @Test
    void shouldCopyResourceValidation()
    {
        WebPageResourceValidation validation = new WebPageResourceValidation("error-message");
        validation.setCssSelector("css-selector");
        validation.setPageURL("page-url");
        validation.setStatusCode(OptionalInt.of(200));

        WebPageResourceValidation copy = validation.copy();

        assertEquals(validation.getCssSelector(), copy.getCssSelector());
        assertEquals(validation.getPageURL(), copy.getPageURL());
        assertEquals(validation.getUriOrError(), copy.getUriOrError());
        assertEquals(validation.getStatusCode(), copy.getStatusCode());
    }
}
