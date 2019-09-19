Description: Integration tests for ResourceCheckStes class

Meta:
    @group vividus-plugin-rest-to-web-api

Lifecycle:
Examples:
|pageToValidate          |
|https://www.example.com/|

Scenario: Verification of Then all resources by selector $cssSelector from $html are valid; Source from HTTP response
When I issue a HTTP GET request for a resource with the URL '<pageToValidate>'
Then all resources by selector `a` from ${response} are valid

Scenario: Verification of Then all resources by selector $cssSelector are valid on:$pages
Then all resources by selector `a` are valid on:
|pages                                       |
|<pageToValidate>                            |

Scenario: Verification of Then all resources by selector $cssSelector from $html are valid; Source from WEB page
Given I am on a page with the URL '<pageToValidate>'
Then all resources by selector `a` from ${pageSource} are valid
