Description: Integration tests for ProxySteps class.

Meta:
    @epic vividus-plugin-web-app
    @feature proxy
    @proxy

Scenario: Verify step Then number of HTTP $httpMethods requests with URL pattern `$urlPattern` is $comparisonRule `$number`
Given I am on a page with the URL 'https://httpbin.org/get'
Then number of HTTP GET requests with URL pattern `https://httpbin\.org/get` is EQUAL TO `1`

Scenario: Verify step When I clear proxy log
Given I am on a page with the URL 'http:/httpbin.org/get'
When I clear proxy log
Then number of HTTP GET, POST requests with URL pattern `http://httpbin\.org/get` is EQUAL TO `0`

Scenario: Verify steps "When I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save URL to $scopes variable `$variableName`" and  "When I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save URL query to $scopes variable `$variableName`"
Meta:
    @issueId 1248
Given I am on a page with the URL 'https://www.google.com/search?q=vividus'
When I capture HTTP GET or POST request with URL pattern `.*/search.*=vividus` and save URL to scenario variable `URL`
Then `${URL}` is equal to `https://www.google.com/search?q=vividus`
When I capture HTTP GET request with URL pattern `.*/search.*=vividus` and save URL query to scenario variable `query`
Then `${query.q[0]}` is equal to `vividus`
Then `${query.q}` is equal to `[vividus]`
Then `${query}` is equal to `{q=[vividus]}`

Scenario: Verify step When I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save request data to $scopes variable `$variableName`
Given I am on a page with the URL 'http://httpbin.org/forms/post'
When I click on element located `By.xpath(//button)`
When I capture HTTP POST request with URL pattern `http://httpbin\.org/post` and save request data to SCENARIO variable `requestData`
Then `${requestData.query}` is equal to `{}`
Then `${requestData.requestBodyParameters}` is equal to `{delivery=[], custtel=[], comments=[], custemail=[], custname=[]}`
Then `${requestData.requestBody}` is not equal to `null`
Then `${requestData.responseStatus}` is equal to `200`

Scenario: Verify step When I wait until HTTP $httpMethods request with URL pattern `$urlPattern` exists in proxy log
Given I am on a page with the URL 'http://httpbin.org/get'
When I wait until HTTP GET or POST request with URL pattern `http://httpbin\.org/get` exists in proxy log
Then number of HTTP GET or POST requests with URL pattern `http://httpbin\.org/get` is EQUAL TO `1`

Scenario: Verify step When I add headers to proxied requests with URL pattern which $comparisonRule `$url`:$headers
Meta:
    @requirementId 603
When I add headers to proxied requests with URL pattern which is equal to `http://httpbin.org/headers`:
|name     |value     |
|testName1|testValue1|
|testName2|testValue2|
Given I am on a page with the URL 'http://httpbin.org/headers'
When I change context to element located `xpath(//pre)`
When I set the text found in search context to the 'SCENARIO' variable 'response'
Then JSON element from `${response}` by JSON path `$.headers` is equal to `
{
    "Testname1": "testValue1",
    "Testname2": "testValue2"
}
`IGNORING_EXTRA_FIELDS

Scenario: Verify step When I mock HTTP responses with request URL which $comparisonRule `$url` using response code `$responseCode`, content `$payload` and headers:$headers with binary data
Meta:
    @requirementId 1104
Given I am on a page with the URL '${vividus-test-site-url}/frames.html'
When I switch to frame located `<frameId>`
Then number of elements found by `<elementId>` is = `0`
When I mock HTTP responses with request URL which CONTAINS `example.com` using response code `200`, content `#{loadBinaryResource(page.html)}` and headers:
|name        |value    |
|Content-Type|text/html|
When I refresh the page
When I switch to frame located `<frameId>`
Then number of elements found by `<elementId>` is = `1`
Examples:
|frameId       |elementId|
|id(exampleCom)|id(sw)   |

Scenario: Verify step When I mock HTTP responses with request URL which $comparisonRule `$url` using response code `$responseCode`, content `$payload` and headers:$headers
Meta:
    @requirementId 1104
When I mock HTTP responses with request URL which CONTAINS `frames.html` using response code `200`, content `#{loadResource(page.html)}` and headers:
|name        |value    |
|Content-Type|text/html|
Given I am on a page with the URL '${vividus-test-site-url}/frames.html'
Then number of elements found by `id(sw)` is = `1`

Scenario: Verify step When I mock HTTP responses with request URL which $comparisonRule `$url` using response code `$responseCode` and headers:$headers
Meta:
    @requirementId 1104
Given I am on a page with the URL '${vividus-test-site-url}/frames.html'
When I switch to frame located `<frameId>`
Then number of elements found by `<elementSelector>` is = `1`
When I mock HTTP responses with request URL which CONTAINS `example.com` using response code `404` and headers:
|name          |value|
|Content-Length|0    |
When I refresh the page
When I switch to frame located `<frameId>`
Then number of elements found by `<elementSelector>` is = `0`
Examples:
|frameId       |elementSelector|
|id(exampleCom)|cssSelector(h1)|

Scenario: Verify step When I clear proxy mocks
Meta:
    @requirementId 1176
When I mock HTTP responses with request URL which CONTAINS `frames.html` using response code `200`, content `#{loadResource(page.html)}` and headers:
|name        |value    |
|Content-Type|text/html|
When I clear proxy mocks
Given I am on a page with the URL '${vividus-test-site-url}/frames.html'
Then number of elements found by `id(sw)` is = `0`
