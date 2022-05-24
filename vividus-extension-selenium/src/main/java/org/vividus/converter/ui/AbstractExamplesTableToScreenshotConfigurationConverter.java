/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.converter.ui;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.vividus.ui.screenshot.ScreenshotConfiguration;

public abstract class AbstractExamplesTableToScreenshotConfigurationConverter<T extends ScreenshotConfiguration>
        extends FunctionalParameterConverter<ExamplesTable, ScreenshotConfiguration>
{
    protected AbstractExamplesTableToScreenshotConfigurationConverter(Class<T> clazz)
    {
        super(examplesTable -> covert(clazz, examplesTable));
    }

    private static <T extends ScreenshotConfiguration> T covert(Class<T> clazz, ExamplesTable examplesTable)
    {
        List<T> configurations = examplesTable.getRowsAs(clazz);
        Validate.isTrue(configurations.size() == 1, "Only one row is acceptable for screenshot configurations");
        return configurations.get(0);
    }
}
