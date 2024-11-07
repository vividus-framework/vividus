/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.report.allure.plugin;

import java.io.IOException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class BrandTitlePlugin extends DynamicPlugin
{
    private static final String CSS_TEMPLATE = """
            .side-nav__brand span {%n\
              display: none;%n\
            }%n\
            .side-nav__brand:after {%n\
              content: '%s';%n\
              padding-left: 15px;%n\
              font-size: 22px;%n\
              font-weight: normal;%n\
            }%n\
            .side-nav__footer :last-child .side-nav__collapse .side-nav__text::after {%n\
              content: 'Powered by Allure';%n\
              color: #999;%n\
              display: block;%n\
              width: 100%%;%n\
              text-align: center;%n\
              padding-top: 20px;%n\
              font-size: 11px;%n\
            }""";

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public BrandTitlePlugin(String brandTitle) throws IOException
    {
        super("brand-title", "styles.css", () -> List.of(CSS_TEMPLATE.formatted(brandTitle)));
    }
}
