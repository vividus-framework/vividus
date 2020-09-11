Description: Integration tests for mobile app steps

Meta:
    @epic vividus-plugin-mobile-app

Lifecycle:
Examples:
/data/tables/system/mobile_app/locators/${target-platform}.table

Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Meta:
    @healthCheck
Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|


Scenario: Verify step: 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`' and Appium XPath Locator
Then number of elements found by `xpath(<mainViewHeaderTextXpath>)` is equal to `1`


Scenario: Verify step: 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`' and Accessibility Id Locator
Then number of VISIBLE elements found by `accessibilityId(<togglerAccessibilityId>):a` is equal to `1`


Scenario: Verify step: 'When I tap on element located `$locator` with duration `$duration`'
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `0`
When I tap on element located `accessibilityId(<togglerAccessibilityId>)` with duration `PT0.5S`
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `1`


Scenario: Verify step: 'When I tap on element located `$locator`'
Then number of elements found by `accessibilityId(<incrementAccessibilityId>)` is equal to `0`
When I tap on element located `xpath(<menuButtonXpath>)`
Then number of elements found by `accessibilityId(<incrementAccessibilityId>)` is equal to `1`
