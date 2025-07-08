Meta:
    @epic vividus-plugin-web-app-to-rest-api

Scenario: Validate cross-domain cookie sharing between web-application and REST API
When I execute HTTP GET request for resource with relative URL `/cookies/set?vividus=cookie`
Given I am on main application page
When I set HTTP context cookies to browser
Then cookie with name that is equal to `vividus` is not set
When I execute HTTP GET request for resource with relative URL `/cookies/delete?vividus`

Scenario: Validate cross-domain cookie sharing between web-application and REST API
Meta:
    @playwrightOnly
When I execute HTTP GET request for resource with relative URL `/cookies/set?vividus=cookie`
Given I am on main application page
When I set HTTP context cookies to browser
Then cookie with name that is equal to `vividus` is set
When I execute HTTP GET request for resource with relative URL `/cookies/delete?vividus`
When I remove all cookies from current domain

Scenario: Validate steps for cookie sharing between web-application and REST API
Meta:
    @playwrightSupported
Given I am on page with URL `${http-endpoint}/cookies`
When I execute HTTP GET request for resource with relative URL `/cookies/set?c1=v1`
Then cookie with name that is equal to `c1` is not set
When I set HTTP context cookies to browser
Then cookie with name that is equal to `c1` is set
Then text `v1` exists
When I execute HTTP GET request for resource with relative URL `/cookies/set?c2=v2`
When I set HTTP context cookies to browser without applying changes
Then cookie with name that is equal to `c1` is set
Then cookie with name that is equal to `c2` is set
Then text `v1` exists
Then text `v2` does not exist
When I refresh page
Then text `v2` exists
When I remove all HTTP cookies
When I execute HTTP GET request for resource with relative URL `/cookies`
Then JSON element from `${json-context}` by JSON path `$` is equal to `{}`
When I set browser cookies to HTTP context
When I execute HTTP GET request for resource with relative URL `/cookies`
Then JSON element from `${json-context}` by JSON path `$` is equal to `{"c1":"v1","c2":"v2"}`
