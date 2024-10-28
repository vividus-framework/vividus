package org.vividus.ui.util;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.vividus.selenium.locator.LocatorConverter;
import org.vividus.selenium.locator.RelativeElementPosition;
import org.vividus.ui.action.search.LocatorType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class RelativeLocatorUtils
{
    public static final Pattern ITEM_TO_FIND_LOCATOR_PATTERN = Pattern.compile("^(\\w+\\([^)]+\\))");
    public static final Pattern RELATIVE_LOCATORS_PATTERN = Pattern.compile("\\.(\\w+)\\((\\w+\\([^)]+\\))\\)");

    private RelativeLocatorUtils()
    {
    }

    public static By convertRelativeStringToBy(String searchParams)
    {
        Matcher itemToFindMatcher = ITEM_TO_FIND_LOCATOR_PATTERN.matcher(searchParams);
        if (itemToFindMatcher.find())
        {
            final String itemToFindLocatorString = itemToFindMatcher.group(0);
            String relativePart = searchParams.substring(itemToFindLocatorString.length());

            Map<String, String> relativeLocators = getRelativeLocators(relativePart);

            RelativeLocator.RelativeBy relativeBy = RelativeLocator.with(By.xpath(""));

            for (Map.Entry<String, String> relativeLocator : getRelativeLocators(relativePart).entrySet())
            {
                RelativeElementPosition relativeElementPosition = findRelativePosition(relativeLocator.getValue());
                relativeBy = relativeElementPosition.apply(relativeBy, By.xpath("dummy"));
            }

            return relativeBy;
        }
        throw new IllegalArgumentException("Invalid relative locator format");
    }

    private static Map<String, String> getRelativeLocators(String relativePart)
    {
        Matcher relativeMatcher = RELATIVE_LOCATORS_PATTERN.matcher(relativePart);
        Map<String, String> relativeLocators = new LinkedHashMap<>();
        while (relativeMatcher.find())
        {
            String action = relativeMatcher.group(1);
            String locator = relativeMatcher.group(2);
            relativeLocators.put(locator, action);
        }
        return relativeLocators;
    }

    private static RelativeElementPosition findRelativePosition(String relativePosition)
    {
        String typeInLowerCase = relativePosition.toLowerCase();
        return Stream.of(RelativeElementPosition.values())
                .filter(t -> StringUtils.replace(t.name().toLowerCase(), "_", "")
                        .equals(typeInLowerCase))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        String.format("Unsupported relative element position: %s", relativePosition)));
    }
}
