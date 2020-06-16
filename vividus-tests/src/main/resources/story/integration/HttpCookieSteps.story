Description: Integration tests for HttpCookieSteps functionality

Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify step: "When I save value of HTTP cookie with name $cookieName to $scopes variable $variableName"
When I send HTTP GET to the relative URL '/cookies/set?cookieName=cookieValue'
When I save value of HTTP cookie with name `cookieName` to SCENARIO variable `value`
Then `${value}` is equal to `cookieValue`

Scenario: Verify step: "I change value of all HTTP cookies with name `$cookieName` to `$newCookieValue`"
When I send HTTP GET to the relative URL '/cookies/set?name=cookieValue'
When I change value of all HTTP cookies with name `name` to `newCookieValue`
When I save value of HTTP cookie with name `name` to SCENARIO variable `value`
Then `${value}` is equal to `newCookieValue`
