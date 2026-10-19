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

package org.vividus.ui.web.action.search;

final class CaseInsensitiveXpaths
{
    private static final String LATIN_ONLY_ARGUMENTS = "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'";
    private static final String ALL_ALPHABETS_ARGUMENTS = alphabetsArguments();

    private CaseInsensitiveXpaths()
    {
    }

    /**
     * Expected XPaths in the tests are written with the Latin alphabet only, this adds the other supported
     * alphabets to the translate() arguments the same way the production code does.
     *
     * @param xpath the XPath with the Latin-only translate() arguments
     * @return the XPath with the arguments of the production translate() call
     */
    static String withAllAlphabets(String xpath)
    {
        return xpath.replace(LATIN_ONLY_ARGUMENTS, ALL_ALPHABETS_ARGUMENTS);
    }

    private static String alphabetsArguments()
    {
        String translate = String.format(AbstractWebElementSearchAction.TRANSLATE_TO_LOWER_CASE, ".");
        // translate(., 'UPPER', 'lower') -> 'UPPER', 'lower'
        return translate.substring("translate(.,".length() + 1, translate.length() - 1);
    }
}
