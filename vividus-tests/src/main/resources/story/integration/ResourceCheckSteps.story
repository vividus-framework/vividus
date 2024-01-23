Description: Integration tests for ResourceCheckStes class

Meta:
    @epic vividus-plugin-rest-to-web-api

Lifecycle:
Examples:
|pageToValidate                     |
|${vividus-test-site-url}/links.html|

Scenario: Steps verification 'Then all resources by selector $cssSelector from $html are valid', 'Then all resources found by $locatorType `$locator` in $html are valid'; Source from HTTP response
When I execute HTTP GET request for resource with URL `<pageToValidate>`
Then all resources found by CSS selector `a[href]` in ${response} are valid
!-- Deprecated
Then all resources by selector `a[href]` from ${response} are valid

Scenario: Steps verification 'Then all resources by selector $cssSelector are valid on:$pages', 'Then all resources found by $locatorType `$locator` are valid on:$pages'
Then all resources found by CSS selector `a[href], img` are valid on:
|pages           |
|<pageToValidate>|
!-- Deprecated
Then all resources by selector `a[href], img` are valid on:
|pages           |
|<pageToValidate>|

Scenario: Steps verification 'Then all resources by selector $cssSelector from $html are valid', 'Then all resources found by $locatorType `$locator` in $html are valid'; Source from WEB page
Given I am on page with URL `<pageToValidate>`
Then all resources found by xpath `//a[@href]` in ${${source-code}} are valid
!-- Deprecated
Then all resources by selector `a[href]` from ${source-code} are valid
When I change context to element located by `linkText(Link to unexistent element)`
Then all resources by selector `a[href]` from ${context-source-code} are valid

Scenario: Verification of the URI exclusion mechanism via 'resource-checker.uri-to-ignore-regex' property
Given I am on page with URL `${vividus-test-site-url}/visualTestIntegration.html`
When I change context to element located by `linkText(Home)`
Then all resources found by CSS selector `a` in ${context-source-code} are valid
