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

package org.vividus.bdd.variable.ui.web;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.variable.DynamicVariable;
import org.vividus.bdd.variable.ui.SourceCodeDynamicVariable;

@Named("page-source")
@Deprecated(forRemoval = true, since = "0.3.10")
public class PageSourceDynamicVariable implements DynamicVariable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSourceDynamicVariable.class);
    private final SourceCodeDynamicVariable appSourceVariable;

    public PageSourceDynamicVariable(SourceCodeDynamicVariable appSourceVariable)
    {
        this.appSourceVariable = appSourceVariable;
    }

    @Override
    public String getValue()
    {
        LOGGER.atError().log(
            "Dynamice variable `page-source` is deprecated and will be removed in VIVIDUS 0.4.0, please use "
            + "`source-code` instead");
        return appSourceVariable.getValue();
    }
}
