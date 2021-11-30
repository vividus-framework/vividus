Description: Integration tests for mobile app steps

Meta:
    @epic vividus-plugin-mobile-app
    @feature proxy
    @proxy

Lifecycle:
Examples:
/data/tables/system/mobile_app/locators/${target-platform}.table


Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|

Scenario: Verify step: 'When I activate application with bundle identifier `$bundleId`'
When I activate application with bundle identifier `${browser-app}`
When I wait until element located `accessibilityId(<togglerAccessibilityId>)` disappears
When I activate application with bundle identifier `${main-app}`
When I wait until element located `accessibilityId(<togglerAccessibilityId>)` appears
Then number of HTTP GET requests with URL pattern `.*` is not equal to `0`

Scenario: Verify step: 'When I close mobile application'
When I close mobile application
