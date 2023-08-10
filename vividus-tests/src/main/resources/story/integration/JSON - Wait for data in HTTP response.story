Description: Integration tests for JsonResponseValidationSteps class

Meta:
    @epic vividus-plugin-rest-api; vividus-plugin-json

Lifecycle:
Before:
Scope: STORY
When I execute HTTP GET request for resource with URL `https://raw.githubusercontent.com/json-path/JsonPath/master/json-path-web-test/src/main/resources/webapp/json/goessner.json`
Then `${responseCode}` matches `20\d`


Scenario: Verify step 'When I save JSON element from `$json` by JSON path `$jsonPath` to $scopes variable `$variableName`'
When I save JSON element from `${response}` by JSON path `$.store.book.length()` to scenario variable `numberOfBooks`
Then `${numberOfBooks}` is equal to `4`


Scenario: Verify step "When I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times$stepsToExecute"
When I wait for presence of element by `$.json.iteration3` for `PT2M` duration retrying 3 times
|step                                                                                                            |
|Given I initialize scenario variable `iteration` with value `#{eval(${iteration:0} + 1)}`                       |
|When I set request headers:                                                                                     |
|{headerSeparator=!,valueSeparator=!}                                                                            |
|!name         !value            !                                                                               |
|!Content-Type !application/json !                                                                               |
|Given request body: {                                                                                           |
|  "iteration${iteration}": ${iteration}                                                                         |
|}                                                                                                               |
|When I execute HTTP POST request for resource with relative URL `/post`                                         |
|Then JSON element from `${json-context}` by JSON path `$.headers.Content-Type` is equal to `"application/json"` |

Scenario: Verify failure in step "When I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times$stepsToExecute"
When I wait for presence of element by `$.non-existing` for `PT1S` duration retrying 1 times
|step                                                                        |
|When I execute HTTP GET request for resource with relative URL `status/204` |
