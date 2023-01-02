Meta: @proxy

Scenario: BrowserStack Local Healthcheck
Given I am on page with URL `https://example.com`
Then text `Example Domain` exists
Then number of HTTP GET requests with URL pattern `.*example\.com/$` is greater than `0`
