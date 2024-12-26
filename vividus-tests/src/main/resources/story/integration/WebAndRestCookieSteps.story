Meta:
    @epic vividus-plugin-web-app-to-rest-api

Scenario: Validate steps for cookie sharing between web-application and REST API
Meta:
    @playwrightSupported
Given I am on page with URL `https://httpbingo.org/cookies`
When I execute HTTP GET request for resource with URL `https://httpbingo.org/cookies/set?c1=v1`
Then cookie with name that is equal to `c1` is not set
When I set HTTP context cookies to browser
Then cookie with name that is equal to `c1` is set
Then text `v1` exists
When I execute HTTP GET request for resource with URL `https://httpbingo.org/cookies/set?c2=v2`
When I set HTTP context cookies to browser without applying changes
Then cookie with name that is equal to `c1` is set
Then cookie with name that is equal to `c2` is set
Then text `v1` exists
Then text `v2` does not exist
