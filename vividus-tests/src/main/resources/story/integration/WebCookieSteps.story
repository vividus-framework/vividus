Description: Integration tests for cookie management in web application

Meta:
    @epic vividus-plugin-web-app

Scenario: Verify step: "When I set all cookies for current domain:$parameters"
Given I am on the main application page
When I set all cookies for current domain:
|cookieName |cookieValue |path|
|cookieName1|cookieValue1|/   |
Then a cookie with the name 'cookieName1' is set

Scenario: Verify step: "When I remove a cookie with the name '$cookieName' from the current domain"
Given I am on the main application page
When I set all cookies for current domain:
|cookieName |cookieValue |path|
|cookieName2|cookieValue2|/   |
When I remove a cookie with the name 'cookieName2' from the current domain
Then a cookie with the name 'cookieName2' is not set
