/*
 * Copyright 2019-2025 the original author or authors.
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
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class BrandLogoPlugin extends DynamicPlugin
{
    private static final String CSS_TEMPLATE = """
            .side-nav__brand {%n\
              background: url('%s') no-repeat left center !important;%n\
              background-size: 44px 44px !important;%n\
            }""";

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public BrandLogoPlugin(String logoPath, PluginFilesLoader pluginFilesLoader) throws IOException
    {
        super("brand-logo", "styles.css", () -> List.of(CSS_TEMPLATE.formatted(FilenameUtils.getName(logoPath))));
        Path brandLogo = pluginFilesLoader.loadResource(logoPath);
        getPluginFiles().put(brandLogo.getFileName().toString(), brandLogo);
    }
}
