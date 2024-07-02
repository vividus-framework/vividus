Meta:
    @epic vividus-plugin-web-app

Scenario: Validate steps for cookie management in web application
Given I am on the main application page

When I set all cookies for current domain without applying changes:
|cookieName |cookieValue |path|
|cookieName1|cookieValue1|/   |
Then cookie with name `cookieName1` is set
Then cookie with name that matches `.*Name1` is set
When I remove cookie with name `cookieName1` from current domain
Then cookie with name `cookieName1` is not set
Then cookie with name that is equal to `cookieName1` is not set

When I set all cookies for current domain:
|cookieName |cookieValue |path|
|cookieName2|cookieValue2|/   |
|cookieName3|cookieValue3|/   |
When I remove cookie with name `cookieName2` from current domain without applying changes
Then cookie with name `cookieName2` is not set
When I remove all cookies from current domain
Then cookie with name `cookieName3` is not set
