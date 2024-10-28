package org.vividus.selenium.locator;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

public enum RelativeElementPosition
{
    ABOVE
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, By locator)
        {
            return relativeBy.above(locator);
        }

        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.above(element);
        }
    },
    BELOW
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, By locator)
        {
            return relativeBy.below(locator);
        }

        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.below(element);
        }
    },
    TO_LEFT_OF
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, By locator)
        {
            return relativeBy.toLeftOf(locator);
        }

        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.toLeftOf(element);
        }
    },
    TO_RIGHT_OF
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, By locator)
        {
            return relativeBy.toRightOf(locator);
        }

        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.toRightOf(element);
        }
    },
    NEAR
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, By locator)
        {
            return relativeBy.near(locator);
        }

        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.near(element);
        }
    };

    public abstract RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, By locator);
    public abstract RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element);
}
