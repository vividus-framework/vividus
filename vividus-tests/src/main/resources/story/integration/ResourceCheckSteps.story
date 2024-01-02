Description: Integration tests for ResourceCheckStes class

Meta:
    @epic vividus-plugin-rest-to-web-api

Lifecycle:
Examples:
|pageToValidate                     |
|${vividus-test-site-url}/links.html|

Scenario: Steps verification 'Then all resources by selector $cssSelector from $html are valid', 'Then all resources found by $locatorType `$locator` in $html are valid'; Source from HTTP response
When I execute HTTP GET request for resource with URL `<pageToValidate>`
Then all resources found by CSS selector `a` in ${response} are valid
!-- Deprecated
Then all resources by selector `a` from ${response} are valid

Scenario: Steps verification 'Then all resources by selector $cssSelector are valid on:$pages', 'Then all resources found by $locatorType `$locator` are valid on:$pages'
Then all resources found by CSS selector `a, img` are valid on:
|pages           |
|<pageToValidate>|
!-- Deprecated
Then all resources by selector `a, img` are valid on:
|pages           |
|<pageToValidate>|

Scenario: Steps verification 'Then all resources by selector $cssSelector from $html are valid', 'Then all resources found by $locatorType `$locator` in $html are valid'; Source from WEB page
Given I am on page with URL `<pageToValidate>`
Then all resources found by xpath `//a` in ${${source-code}} are valid
!-- Deprecated
Then all resources by selector `a` from ${source-code} are valid
