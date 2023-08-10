Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify step: "Then response does not contain body"
When I execute HTTP GET request for resource with URL `${vividus-test-site-url}/api/no-content`
Then response does not contain body


Scenario: Verify step: "Then response code is $comparisonRule `$responseCode`"
When I execute HTTP GET request for resource with relative URL `/status/<statusCode>`
Then response code is equal to `<statusCode>`
Examples:
|statusCode|
|200       |
|404       |


Scenario: Verify steps: "Then response body $validationRule resource at `$resourcePath`", "Then size of decompressed response body is equal to `8090`", "Then content type of response body $comparisonRule `$contentType`"
When I execute HTTP GET request for resource with relative URL `/image/png`
Then size of decompressed response body is equal to `8090`
Then response body is equal to resource at `/data/pig`
Then content type of response body is equal to `image/png`


Scenario: Verify steps: "Then connection is secured using $securityProtocol protocol", "Then response time is $comparisonRule `$responseTime` milliseconds"
When I execute HTTP GET request for resource with URL `${vividus-test-site-url}/index.html`
Then connection is secured using TLSv1.3 protocol
Then response time is less than `5000` milliseconds


Scenario: Verify step: "When I wait for response code `$responseCode` for `$duration` duration retrying $retryTimes times$stepsToExecute"
Given I initialize scenario variable `relativeURL` with value `get-wrong-wrong-wrong`
When I wait for response code `200` for `PT2M` duration retrying 3 times
|step                                                                                                                      |
|Given I initialize scenario variable `relativeURL` with value `#{eval(stringUtils:substringBeforeLast(relativeURL, '-'))}`|
|When I execute HTTP GET request for resource with relative URL `${relativeURL}`                                           |
Then `${responseCode}` is equal to `200`


Scenario: Verify steps: "Then number of response headers with name `$headerName` is $comparisonRule $number", "When I save response header `$headerName` value to $scopes variable `$variableName`", "Then value of response header `$headerName` $comparisonRule `$value`", "Then response header `$headerName` contains elements:$elements"
When I execute HTTP GET request for resource with relative URL `/html`
Then number of response headers with name `content-type` is equal to 1
When I save response header `content-type` value to scenario variable `contentType`
Then `${contentType}` is equal to `text/html; charset=utf-8`
Then value of response header `content-type` is equal to `text/html; charset=utf-8`
Then response header `content-type` contains elements:
|element  |
|text/html|
