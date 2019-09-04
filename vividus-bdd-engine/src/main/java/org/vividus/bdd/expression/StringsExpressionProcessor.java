/*
 * Copyright 2019 the original author or authors.
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;

import com.github.javafaker.Faker;

import org.apache.commons.lang3.StringUtils;
import org.vividus.util.ILocationProvider;
import org.vividus.util.ResourceUtils;

@Named
public class StringsExpressionProcessor implements IExpressionProcessor
{
    private static final String COMMA = ",";

    @Inject private ILocationProvider locationProvider;

    private final List<IExpressionProcessor> unaryExpressions = List.of(
            new UnaryExpressionProcessor("trim",              StringUtils::trim),
            new UnaryExpressionProcessor("toLowerCase",       StringUtils::lowerCase),
            new UnaryExpressionProcessor("toUpperCase",       StringUtils::upperCase),
            new UnaryExpressionProcessor("capitalize",        StringUtils::capitalize),
            new UnaryExpressionProcessor("uncapitalize",      StringUtils::uncapitalize),
            new UnaryExpressionProcessor("generate",          this::generate),
            new UnaryExpressionProcessor("generateLocalized", this::generateLocalized),
            new UnaryExpressionProcessor("encodeUrl",         input -> URLEncoder.encode(input, UTF_8)),
            new UnaryExpressionProcessor("loadResource",      ResourceUtils::loadResource),
            new UnaryExpressionProcessor("resourceToBase64",  input -> Base64.getEncoder()
                    .encodeToString(ResourceUtils.loadResourceAsByteArray(input))),
            new UnaryExpressionProcessor("decodeFromBase64",  input -> new String(Base64.getDecoder()
                    .decode(input.getBytes(UTF_8)), UTF_8)),
            new UnaryExpressionProcessor("encodeToBase64",    input -> new String(Base64.getEncoder()
                    .encode(input.getBytes(UTF_8)), UTF_8)));

    @Override
    public Optional<String> execute(String expression)
    {
        return unaryExpressions.stream()
            .map(processor -> processor.execute(expression))
            .filter(Optional::isPresent)
            .findFirst()
            .orElseGet(Optional::empty);
    }

    private String generate(String input)
    {
        return generate(locationProvider.getLocale(), input);
    }

    private String generateLocalized(String input)
    {
        String inputPart = StringUtils.substringBeforeLast(input, COMMA);
        String[] localeParts = StringUtils.split(StringUtils.substringAfterLast(input, COMMA), '-');
        Locale locale = localeParts.length == 2 ? new Locale(localeParts[0].trim(), localeParts[1].trim()) : new Locale(
                localeParts[0].trim());
        return generate(locale, inputPart);
    }

    private static String generate(Locale locale, String input)
    {
        return new Faker(locale).expression(String.format("#{%s}", input));
    }
}
