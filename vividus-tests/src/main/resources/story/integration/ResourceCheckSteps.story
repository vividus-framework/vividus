Description: Integration tests for ResourceCheckStes class

Meta:
    @epic vividus-plugin-rest-to-web-api

Lifecycle:
Examples:
|pageToValidate          |
|${vividus-test-site-url}|

Scenario: Verification of Then all resources by selector $cssSelector from $html are valid; Source from HTTP response
When I execute HTTP GET request for resource with URL `<pageToValidate>`
Then all resources by selector `a` from ${response} are valid

Scenario: Verification of Then all resources by selector $cssSelector are valid on:$pages
Then all resources by selector `a, img` are valid on:
|pages                                       |
|<pageToValidate>                            |

Scenario: Verification of Then all resources by selector $cssSelector from $html are valid; Source from WEB page
Given I am on page with URL `<pageToValidate>`
Then all resources by selector `a` from ${source-code} are valid
