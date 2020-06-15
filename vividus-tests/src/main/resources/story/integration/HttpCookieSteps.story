Description: Integration tests for HttpCookieSteps functionality

Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify step: "When I save value of HTTP cookie with name $cookieName to $scopes variable $variableName"
When I send HTTP GET to the relative URL '/cookies/set?cookieName=cookieValue'
When I save value of HTTP cookie with name `cookieName` to SCENARIO variable `value`
Then `${value}` is equal to `cookieValue`
