Meta: @proxy

Scenario: BrowserStack Local Healthcheck
Given I am on page with URL `http://bs-local.com:45454/check`
Then text `Up and running` exists
Then number of HTTP GET requests with URL pattern `http://bs-local\.com:45454/check` is greater than `0`
