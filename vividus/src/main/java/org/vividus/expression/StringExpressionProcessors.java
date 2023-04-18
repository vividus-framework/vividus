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

package org.vividus.expression;

import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.jbehave.core.expressions.BiArgExpressionProcessor;
import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.MultiArgExpressionProcessor;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.vividus.util.ILocationProvider;
import org.vividus.util.ResourceUtils;

import net.datafaker.Faker;

public class StringExpressionProcessors extends DelegatingExpressionProcessor
{
    private static final String COMMA = ",";

    private static final LoadingCache<Locale, Faker> FAKERS = CacheBuilder.newBuilder().build(new CacheLoader<>()
    {
        @Override
        public Faker load(Locale locale)
        {
            return new Faker(locale);
        }
    });

    @SuppressWarnings("checkstyle:SingleSpaceSeparator")
    public StringExpressionProcessors(ILocationProvider locationProvider)
    {
        super(List.of(
            new SingleArgExpressionProcessor<>("trim",                  StringUtils::trim),
            new SingleArgExpressionProcessor<>("toLowerCase",           StringUtils::lowerCase),
            new SingleArgExpressionProcessor<>("toUpperCase",           StringUtils::upperCase),
            new SingleArgExpressionProcessor<>("capitalizeFirstWord",   StringUtils::capitalize),
            new SingleArgExpressionProcessor<>("capitalizeWords",       WordUtils::capitalize),
            new SingleArgExpressionProcessor<>("capitalizeWordsFully",  WordUtils::capitalizeFully),
            new SingleArgExpressionProcessor<>("uncapitalizeFirstWord", StringUtils::uncapitalize),
            new SingleArgExpressionProcessor<>("uncapitalizeWords",     WordUtils::uncapitalize),
            new SingleArgExpressionProcessor<>("generate",              input -> generate(locationProvider.getLocale(),
                    input)),
            new SingleArgExpressionProcessor<>("generateLocalized",     generateLocalized()),
            new SingleArgExpressionProcessor<>("loadResource",          ResourceUtils::loadResource),
            new SingleArgExpressionProcessor<>("escapeHTML",            StringEscapeUtils::escapeHtml4),
            new SingleArgExpressionProcessor<>("escapeJSON",            StringEscapeUtils::escapeJson),
            new SingleArgExpressionProcessor<>("quoteRegExp",           Pattern::quote),
            new BiArgExpressionProcessor<>("substringBefore",           StringUtils::substringBefore),
            new BiArgExpressionProcessor<>("substringAfter",            StringUtils::substringAfter),
            new MultiArgExpressionProcessor<>("anyOf", 0, Integer.MAX_VALUE, StringExpressionProcessors::anyOf)
        ));
    }

    private static String anyOf(List<String> arguments)
    {
        int length = arguments.size();
        return length == 0 ? "" : arguments.get(RandomUtils.nextInt(0, length));
    }

    private static UnaryOperator<String> generateLocalized()
    {
        return input ->
        {
            String inputPart = StringUtils.substringBeforeLast(input, COMMA);
            String[] localeParts = StringUtils.split(StringUtils.substringAfterLast(input, COMMA), '-');
            Locale locale = localeParts.length == 2 ? new Locale(localeParts[0].trim(), localeParts[1].trim())
                    : new Locale(localeParts[0].trim());
            return generate(locale, inputPart);
        };
    }

    private static String generate(Locale locale, String input)
    {
        return FAKERS.getUnchecked(locale).expression(String.format("#{%s}", input));
    }
}
