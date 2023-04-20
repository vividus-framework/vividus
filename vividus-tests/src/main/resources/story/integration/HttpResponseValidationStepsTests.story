Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify step: "Then response does not contain body"
When I execute HTTP GET request for resource with URL `${vividus-test-site-url}/api/no-content`
Then response does not contain body
!-- Deprecated
Then the response does not contain body


Scenario: Verify deprecated steps: "Then the response body $comparisonRule '$content'" & "When I save response body to the $scopes variable '$variableName'"
When I execute HTTP GET request for resource with URL `https://httpbin.org/xml`
Then the response body contains 'Date of publication'
When I save response body to the scenario variable 'my-super-response'
Then `${my-super-response}` matches `.*Date of publication.*`

Scenario: Verify step: "Then response code is $comparisonRule `$responseCode`"
When I execute HTTP GET request for resource with URL `https://httpbin.org/status/<statusCode>`
Then response code is equal to `<statusCode>`
!-- Deprecated
Then the response code is equal to '<statusCode>'
Examples:
|statusCode|
|200       |
|404       |


Scenario: Verify steps: "Then response body $validationRule resource at `$resourcePath`", "Then size of decompressed response body is equal to `8090`", "Then content type of response body $comparisonRule `$contentType`"
When I execute HTTP GET request for resource with URL `https://httpbin.org/image/png`
Then size of decompressed response body is equal to `8090`
Then response body is equal to resource at `/data/pig`
Then content type of response body is equal to `image/png`
!-- Deprecated
Then the response body is equal to resource at '/data/pig'


Scenario: Verify steps: "Then connection is secured using $securityProtocol protocol", "Then response time is $comparisonRule `$responseTime` milliseconds"
When I execute HTTP GET request for resource with URL `${vividus-test-site-url}/index.html`
Then connection is secured using TLSv1.3 protocol
Then response time is less than `5000` milliseconds
!-- Deprecated
Then the connection is secured using TLSv1.3 protocol
Then the response time should be less than '5000' milliseconds


Scenario: Verify step: "When I wait for response code `$responseCode` for `$duration` duration retrying $retryTimes times$stepsToExecute"
Given I initialize scenario variable `relativeURL` with value `get-wrong-wrong-wrong`
When I wait for response code `200` for `PT2M` duration retrying 3 times
|step                                                                                                                      |
|Given I initialize scenario variable `relativeURL` with value `#{eval(stringUtils:substringBeforeLast(relativeURL, '-'))}`|
|When I execute HTTP GET request for resource with relative URL `${relativeURL}`                                           |
Then `${responseCode}` is equal to `200`


Scenario: Verify steps: "Then number of response headers with name `$headerName` is $comparisonRule $number", "When I save response header `$headerName` value to $scopes variable `$variableName`", "Then value of response header `$headerName` $comparisonRule `$value`", "Then response header `$headerName` contains elements:$elements"; verify deprecated HTTP header steps: "Then the number of the response headers with the name '$headerName' is $comparisonRule $value", "When I save response header '$httpHeaderName' value to $scopes variable '$variableName'", "Then the value of the response header '$httpHeaderName' $comparisonRule '$value'", "Then the value of the response header "$httpHeaderName" $comparisonRule "$value"", "Then response header '$httpHeaderName' contains attribute: $attributes"
When I execute HTTP GET request for resource with URL `https://httpbin.org/html`
Then number of response headers with name `Content-Type` is equal to 1
When I save response header `Content-Type` value to scenario variable `contentType`
Then `${contentType}` is equal to `text/html; charset=utf-8`
Then value of response header `Content-Type` is equal to `text/html; charset=utf-8`
Then response header `Content-Type` contains elements:
|element  |
|text/html|
!-- Deprecated
Then the number of the response headers with the name 'Content-Type' is equal to 1
When I save response header 'Content-Type' value to scenario variable 'contentType'
Then `${contentType}` is equal to `text/html; charset=utf-8`
Then the value of the response header 'Content-Type' is equal to 'text/html; charset=utf-8'
Then the value of the response header "Content-Type" is equal to "text/html; charset=utf-8"
Then response header 'Content-Type' contains attribute:
|attribute|
|text/html|
