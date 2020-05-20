Description: Integration tests for ApiSteps functionality

Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify method DEBUG is supported
When I issue a HTTP DEBUG request for a resource with the URL 'http://example.org/'
Then `${responseCode}` is equal to `405`

Scenario: Verify handling of plus character in URL query
When I send HTTP GET to the relative URL '/get?birthDate=<query-parameter-value>'
Then the response code is equal to '200'
Then a JSON element by the JSON path '$.url' is equal to '${http-endpoint}get?birthDate=00:00:00%2B02:00'
Then a JSON element by the JSON path '$.args.birthDate' is equal to '00:00:00+02:00'
Examples:
|query-parameter-value|
|00:00:00+02:00       |
|00:00:00%2B02:00     |

Scenario: Set HTTP cookies
When I send HTTP GET to the relative URL '/cookies/set?vividus-cookie=vividus'
When I send HTTP GET to the relative URL '/cookies'
Then a JSON element by the JSON path '$.cookies' is equal to '{"vividus-cookie": "vividus"}'

Scenario: Verify HTTP cookies are cleared
When I send HTTP GET to the relative URL '/cookies'
Then a JSON element by the JSON path '$.cookies' is equal to '{}'

Scenario: I wait for response code $responseCode for $duration duration retrying $retryTimes times $stepsToExecute
When I initialize the scenario variable `uuid` with value `#{generate(Internet.uuid)}`
When I wait for response code `200` for `PT10S` duration retrying 10 times
|step                                                                                                                        |
|When I issue a HTTP GET request for a resource with the URL '${vividus-test-site-url}/api/delayed-response?clientId=${uuid}'|
Then `${responseCode}` is equal to `200`
