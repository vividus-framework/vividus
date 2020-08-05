Description: Integration tests for ProxySteps class.

Meta:
    @proxy

Scenario: Verify step Then number of HTTP $httpMethod requests with URL pattern `$urlPattern` is $comparisonRule `$number`
Given I am on a page with the URL 'http://httpbin.org/get'
Then number of HTTP GET requests with URL pattern `http://httpbin.org/get` is EQUAL TO `1`

Scenario: Verify step When I clear proxy log
Given I am on a page with the URL 'http:/httpbin.org/get'
When I clear proxy log
Then number of HTTP GET requests with URL pattern `http://httpbin.org/get` is EQUAL TO `0`

Scenario: Verify step When I capture HTTP $httpMethod request with URL pattern `$urlPattern` and save URL query to $scopes variable `$variableName`
Given I am on a page with the URL 'http://httpbin.org/forms/post'
When I click on element located `By.xpath(//button)`
When I capture HTTP POST request with URL pattern `http://httpbin.org/post` and save URL query to SCENARIO variable `query`
Then `${query}` is equal to `{}`

Scenario: Verify step When I capture HTTP $httpMethod request with URL pattern `$urlPattern` and save request data to $scopes variable `$variableName`
Given I am on a page with the URL 'http://httpbin.org/forms/post'
When I click on element located `By.xpath(//button)`
When I capture HTTP POST request with URL pattern `http://httpbin.org/post` and save request data to SCENARIO variable `requestData`
Then `${requestData.query}` is equal to `{}`
Then `${requestData.requestBodyParameters}` is equal to `{delivery=, custtel=, comments=, custemail=, custname=}`
Then `${requestData.requestBody}` is not equal to `null`
Then `${requestData.responseStatus}` is equal to `200`

Scenario: Verify step When I wait until HTTP $httpMethod request with URL pattern `$urlPattern` exists in proxy log
Given I am on a page with the URL 'http://httpbin.org/get'
When I wait until HTTP GET request with URL pattern `http://httpbin.org/get` exists in proxy log
Then number of HTTP GET requests with URL pattern `http://httpbin.org/get` is EQUAL TO `1`

Scenario: Verify step When I add headers to proxied requests with URL pattern which $comparisonRule `$urlPattern`:$headers
When I add headers to proxied requests with URL pattern which is equal to `http://httpbin.org/headers`:
|name     |value     |
|testName1|testValue1|
|testName2|testValue2|
Given I am on a page with the URL 'http://httpbin.org/headers'
When I change context to element located `xpath(//pre)`
When I set the text found in search context to the 'SCENARIO' variable 'response'
Then a JSON element from '${response}' by the JSON path '$.headers' is equal to '
{
    "Testname1": "testValue1",
    "Testname2": "testValue2"
}
'IGNORING_EXTRA_FIELDS
