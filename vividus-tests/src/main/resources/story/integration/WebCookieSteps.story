Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `${http-endpoint}/cookies`

Scenario: Validate steps setting and removing cookies for current domain without applying changes
Meta:
    @playwrightSupported
When I set all cookies for current domain without applying changes:
| cookieName  | cookieValue  | path |
| cookieName1 | cookieValue1 | /    |
Then cookie with name that matches `.*Name1` is set
Then text `cookieValue1` does not exist
When I refresh page
Then text `cookieValue1` exists
When I remove cookie with name `cookieName1` from current domain without applying changes
Then cookie with name that is equal to `cookieName1` is not set
Then text `cookieValue1` exists
When I refresh page
Then text `cookieValue1` does not exist

Scenario: Validate step setting and removing cookies for current domain
Meta:
    @playwrightSupported
Then text `cookieValue2` does not exist
When I set all cookies for current domain:
| cookieName  | cookieValue  | path |
| cookieName2 | cookieValue2 | /    |
Then cookie with name that is equal to `cookieName2` is set
Then text `cookieValue2` exists
When I remove cookie with name `cookieName2` from current domain
Then cookie with name that is equal to `cookieName2` is not set
Then text `cookieValue2` does not exist

Scenario: Validate step setting and removing all cookies for current domain
Meta:
    @playwrightSupported
Then text `cookieValue3` does not exist
When I set all cookies for current domain:
| cookieName  | cookieValue  | path |
| cookieName3 | cookieValue3 | /    |
Then cookie with name that is equal to `cookieName3` is set
Then text `cookieValue3` exists
When I remove all cookies from current domain
Then cookie with name that is equal to `cookieName3` is not set
Then text `cookieValue3` does not exist

Scenario: Validate deprecated steps checking cookies
Then text `cookieValue4` does not exist
When I set all cookies for current domain:
| cookieName  | cookieValue  | path |
| cookieName4 | cookieValue4 | /    |
Then cookie with name `cookieName4` is set
Then text `cookieValue4` exists
When I remove cookie with name `cookieName4` from current domain
Then cookie with name `cookieName4` is not set
Then text `cookieValue4` does not exist
