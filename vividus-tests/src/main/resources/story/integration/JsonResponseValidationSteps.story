Description: Integration tests for JsonResponseValidationSteps class (base website http://jsonpath.herokuapp.com/)

Meta:
    @group vividus-plugin-rest-api

Lifecycle:
Before:
Scope: STORY
When I issue a HTTP GET request for a resource with the URL 'http://jsonpath.herokuapp.com/json/goessner.json'
Then `${responseCode}` matches `20\d`
Examples:
|URL                                             |jsonPath             |booksNumber|
|http://jsonpath.herokuapp.com/json/goessner.json|$.store.book.length()|4          |


Scenario: Verify composite step 'When I save a JSON element from response by JSON path '$jsonPath' to $scopes variable '$variableName''
When I save a JSON element from response by JSON path '<jsonPath>' to scenario variable 'numberOfBooks'
Then `${numberOfBooks}` is equal to `<booksNumber>`


Scenario: Verify composite step 'When I wait for presence of element by '$jsonPath' in HTTP GET response from '$resourceUrl' for '$duration' duration retrying $retryTimes times'
When I wait for presence of element by '<jsonPath>' in HTTP GET response from '<URL>' for 'PT1M' duration retrying 20 times
When I save a JSON element from response by JSON path '<jsonPath>' to scenario variable 'numberOfBooks'
Then `${numberOfBooks}` is equal to `<booksNumber>`

Scenario: Verify composite step 'When I wait for presence of the element by JSON path '$jsonPath' in HTTP GET response from '$resourceUrl' for '$duration' duration'
When I wait for presence of the element by JSON path '<jsonPath>' in HTTP GET response from '<URL>' for 'PT30S' duration
When I save a JSON element from response by JSON path '<jsonPath>' to scenario variable 'numberOfBooks'
Then `${numberOfBooks}` is equal to `<booksNumber>`

Scenario: Verify step "Then a JSON element from '$json' by the JSON path '$jsonPath' is equal to '$expectedData'$options"
When I initialize the scenario variable `current-date` with value `#{generateDate(P, YYYY-MM-DD)}`
Given I initialize the scenario variable `expected-json` using template `/data/json-validation-template.ftl` with parameters:
|currentDate   |
|${current-date}|
Then a JSON element from '
{
    "currentDateWithAnyTime": "${current-date}T22:20:35+07:00",
    "fieldToBeIgnored": "valueToBeIgnored"
}
' by the JSON path '$' is equal to '${expected-json}' ignoring extra fields

Scenario: Verify step "When I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times$stepsToExecute"
When I wait for presence of element by `$.json.iteration3` for `PT15S` duration retrying 3 times
|step                                                                                          |
|When I initialize the step variable `iteration` with value `#{eval(${iteration:0} + 1)}`      |
|When I set request headers:                                                                   |
|{headerSeparator=!,valueSeparator=!}                                                          |
|!name         !value            !                                                             |
|!Content-Type !application/json !                                                             |
|Given request body: {                                                                         |
|  "iteration${iteration}": ${iteration}                                                       |
|}                                                                                             |
|When I send HTTP POST to the relative URL '/post'                                             |
|Then a JSON element by the JSON path '$.headers.Content-Type' is equal to '"application/json"'|
