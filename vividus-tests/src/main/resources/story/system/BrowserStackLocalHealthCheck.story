Meta: @proxy

Scenario: BrowserStack Local Healthcheck
Given I am on a page with the URL 'https://example.com'
Then the text 'Example Domain' exists
Then number of HTTP GET requests with URL pattern `.*example\.com/$` is greater than `0`
