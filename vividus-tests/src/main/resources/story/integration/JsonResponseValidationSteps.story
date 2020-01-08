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
