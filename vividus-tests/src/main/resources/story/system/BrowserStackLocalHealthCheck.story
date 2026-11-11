Meta: @proxy

Scenario: BrowserStack Local Healthcheck
Given I am on page with URL `${vividus-test-site-url}/visualTestIntegration.html`
When I wait until element located by `xpath((.//p)[1])` appears
Then number of HTTP GET requests with URL pattern `.*${vividus-test-site-host}/visualTestIntegration\.html$` is greater than `0`
