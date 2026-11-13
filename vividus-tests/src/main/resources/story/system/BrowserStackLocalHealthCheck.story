Meta: @proxy

Scenario: BrowserStack Local Healthcheck
Given I am on page with URL `${vividus-test-site-url}`
When I wait until element located by `xpath(//img[@name='vividus-logo'])` appears in `PT5M`
Then number of HTTP GET requests with URL pattern `.*${vividus-test-site-host}/$` is greater than `0`
