Meta:
    @epic vividus-plugin-web-app
    @feature proxy

Scenario: Verify the proxy starts when @proxy meta tag is present at scenario level
Meta:
    @proxy
Given I am on page with URL `https://httpbingo.org/get`
Then number of HTTP GET requests with URL pattern `https://httpbingo\.org/get` is EQUAL TO `1`

Scenario: Verify the previous web session is stopped and new is started after the scenario with @proxy meta tag
Given I am on page with URL `https://httpbingo.org`
Then page title contains `HTTP Client Testing Service`
