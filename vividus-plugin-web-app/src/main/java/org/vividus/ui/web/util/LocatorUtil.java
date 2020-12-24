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

package org.vividus.ui.web.util;

import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Quotes;
import org.vividus.selenium.TextUtils;

public final class LocatorUtil
{
    private static final String ANY = "*";
    private static final String CONCAT = "(| )((concat\\([^)]*\\))|('(?!')[^']*')|(\"(?!\\\")[^\"]*\"))";
    private static final Pattern ATTR_VALUE_PATTERN = Pattern
            .compile("((([\\s])([*]))|([.])|(@[\\w.\\-_]*))(| )=" + CONCAT);
    private static final Pattern ATTR_VALUE_TRANSLATE_PATTERN = Pattern
            .compile("(translate\\([^)]*\\))(| )=" + CONCAT);
    private static final Pattern ANY_ATTR_OR_TEXT_VALUE_PATTERN = Pattern.compile("((@[*])|(text\\(\\)))(| )="
            + CONCAT);
    private static final Pattern ATTR_CONTAINS_PATTERN = Pattern.compile("(contains\\((?!normalize-space)(.*?)),");

    private static final String NORMALIZE_SPACE_FORMAT = "$3normalize-space($4$5$6)=$9";
    private static final String ANY_ATTR_OR_TEXT_NORMALIZE_SPACE_FORMAT = "$1[normalize-space()=$6]";
    private static final String NORMALIZE_SPACE_TRANSLATE_FORMAT = "normalize-space($1)=$5$6$7";
    private static final String CONTAINS_NORMALIZE_SPACE_FORMAT = "contains(normalize-space($2),";

    private static final String NORMALIZE_SPACE_WITH_PARAMETR_FORMAT = "normalize-space\\"
            + "((text\\(\\)|\\*|@\\w+|@\\*)\\)(| )=(| )((concat\\([^\\)]*\\))|('[^']*')|(\\\"[^\\\"]*\\\"))";

    private static final Pattern BEFORE_OR_PATTERN = Pattern
            .compile(String.format("(%s)( or )", NORMALIZE_SPACE_WITH_PARAMETR_FORMAT));
    private static final Pattern AFTER_OR_PATTERN = Pattern
            .compile(String.format("( or )(%s)", NORMALIZE_SPACE_WITH_PARAMETR_FORMAT));

    private static final String NORMALIZE_SPACE_FORMAT_BEFORE_OR = "$2[normalize-space()=$6$7$8]$9";
    private static final String NORMALIZE_SPACE_FORMAT_AFTER_OR = "$1$3[normalize-space()=$7$8$9]";

    private static final Pattern SEARCH_IN_CURRENT_NODE_PATTERN = Pattern.compile(
            "(((?<=\\[)[\\w./]+(?=normalize-space))|((?<=\\[)\\./+))" + "([\\w-]*)(\\(?)(@[\\w\\-._]*)(\\)?)");
    private static final String NORMALIZE_CURRENT_NODE_PATTERN = "normalize-space($1$6)";

    private LocatorUtil()
    {
    }

    public static String getXPathByAttribute(String attributeName, String attributeValue)
    {
        return getXPathByTagNameAndAttribute(ANY, attributeName, attributeValue);
    }

    public static String getXPathByTagNameAndAttribute(String tagName, String attributeName, String attributeValue)
    {
        if (!attributeValue.equals(StringEscapeUtils.escapeHtml4(attributeValue)))
        {
            return getXPath(String.format(".//%1$s[@%2$s=%%s or @%2$s=%%s]", tagName, attributeName), attributeValue,
                    StringEscapeUtils.escapeHtml4(attributeValue));
        }
        return getXPath(String.format(".//%s[@%s=%%s]", tagName, attributeName), attributeValue);
    }

    public static By getXPathLocator(String xpathPattern, Object... args)
    {
        return By.xpath(getXPath(xpathPattern, args));
    }

    public static String getXPath(String xpathPattern, Object... args)
    {
        return getXPath(true, xpathPattern, args);
    }

    public static String getXPath(boolean doSpaceNormalization, String xpathPattern, Object... args)
    {
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++)
        {
            String arg = TextUtils.normalizeText(args[i].toString());
            newArgs[i] = Quotes.escape(arg);
        }
        String xpathFromRootPattern = buildXPath(doSpaceNormalization, xpathPattern, newArgs);
        return xpathFromRootPattern.replaceAll("'((concat.*?(\"\\)))|(\"[^\"]*?\"))'", "$1");
    }

    public static By getXPathLocatorByInnerTextWithTagName(String tagName, String text)
    {
        return By.xpath(getXPathByInnerTextWithTagName(tagName, text));
    }

    public static By getXPathLocatorByFullInnerText(String text)
    {
        return By.xpath(getXPath(true, String.format(".//%1$s[.=%%1$s and not(.//%1$s[.=%%1$s])]", ANY), text));
    }

    public static By getXPathLocatorByInnerText(String text)
    {
        return By.xpath(getXPathByInnerTextWithTagName(ANY, text));
    }

    private static String getXPathByInnerTextWithTagName(String tagName, String text)
    {
        return getXPath(true,
                String.format(".//%1$s[contains(., %%1$s) and not(.//%1$s[contains(., %%1$s)])]", tagName), text);
    }

    public static By getXPathLocator(boolean doSpaceNormalization, String xpathPattern, Object... args)
    {
        return By.xpath(buildXPath(doSpaceNormalization, xpathPattern, args));
    }

    private static String buildXPath(boolean doSpaceNormalization, String xpathPattern, Object... args)
    {
        String xpath = args.length > 0 ? String.format(xpathPattern, args) : xpathPattern;
        return doSpaceNormalization ? normalizeXPath(xpath) : xpath;
    }

    private static String normalizeXpathWithOperators(String xpath)
    {
        String newXpath = BEFORE_OR_PATTERN.matcher(xpath).replaceAll(NORMALIZE_SPACE_FORMAT_BEFORE_OR);
        return AFTER_OR_PATTERN.matcher(newXpath).replaceAll(NORMALIZE_SPACE_FORMAT_AFTER_OR);
    }

    private static String normalizeXPath(String xpath)
    {
        String newXpath = ATTR_VALUE_PATTERN.matcher(xpath).replaceAll(NORMALIZE_SPACE_FORMAT);
        newXpath = ANY_ATTR_OR_TEXT_VALUE_PATTERN.matcher(newXpath).replaceAll(ANY_ATTR_OR_TEXT_NORMALIZE_SPACE_FORMAT);
        newXpath = SEARCH_IN_CURRENT_NODE_PATTERN.matcher(newXpath).replaceAll(NORMALIZE_CURRENT_NODE_PATTERN);
        newXpath = normalizeXpathWithOperators(newXpath);
        newXpath = ATTR_VALUE_TRANSLATE_PATTERN.matcher(newXpath).replaceAll(NORMALIZE_SPACE_TRANSLATE_FORMAT);
        return ATTR_CONTAINS_PATTERN.matcher(newXpath).replaceAll(CONTAINS_NORMALIZE_SPACE_FORMAT);
    }
}
