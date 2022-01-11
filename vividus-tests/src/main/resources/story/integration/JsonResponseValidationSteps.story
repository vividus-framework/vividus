Description: Integration tests for JsonResponseValidationSteps class (base website http://jsonpath.herokuapp.com/)

Meta:
    @epic vividus-plugin-rest-api

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

Scenario: Verify composite step 'When I save JSON element from response by JSON path `$jsonPath` to $scopes variable `$variableName`'
When I save JSON element from response by JSON path `<jsonPath>` to scenario variable `numberOfBooks`
Then `${numberOfBooks}` is equal to `<booksNumber>`

Scenario: Verify step "Then JSON element from `$json` by JSON path `$jsonPath` is equal to `$expectedData`$options"
When I initialize the scenario variable `current-date` with value `#{generateDate(P, yyyy-MM-DD)}`
Given I initialize the scenario variable `expected-json` using template `/data/json-validation-template.ftl` with parameters:
|currentDate   |
|${current-date}|
Then JSON element from `
{
    "currentDateWithAnyTime": "${current-date}T22:20:35+07:00",
    "fieldToBeIgnored": "valueToBeIgnored"
}
` by JSON path `$` is equal to `${expected-json}` ignoring extra fields

Scenario: Verify step 'Then number of JSON elements by JSON path `$jsonPath` is $comparisonRule $elementsNumber'
Then number of JSON elements by JSON path `$.store.book` is greater than 0

Scenario: Verify step 'When I set number of elements found by JSON path `$jsonPath` to $scopes variable `$variableName`'
When I set number of elements found by JSON path `$.store.book` to scenario variable `bookCount`
Then `${bookCount}` is greater than `0`

Scenario: Verify step 'When I find $comparisonRule `$elementsNumber` JSON elements by `$jsonPath` and for each element do$stepsToExecute'
When I find greater than `1` JSON elements by `$.store.book` and for each element do
|step                                                       |
|Then number of JSON elements by JSON path `$.author` is = 1|

Scenario: Verify step 'When I find $comparisonRule `$elementsNumber` JSON elements from `$json` by `$jsonPath` and for each element do$stepsToExecute' with zero elements
When I find <= `1` JSON elements from `{}` by `$.name` and for each element do
|step                    |
|Then `0` is equal to `1`|

Scenario: Verify step "When I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times$stepsToExecute"
When I wait for presence of element by `$.json.iteration3` for `PT15S` duration retrying 3 times
|step                                                                                        |
|When I initialize the scenario variable `iteration` with value `#{eval(${iteration:0} + 1)}`|
|When I set request headers:                                                                 |
|{headerSeparator=!,valueSeparator=!}                                                        |
|!name         !value            !                                                           |
|!Content-Type !application/json !                                                           |
|Given request body: {                                                                       |
|  "iteration${iteration}": ${iteration}                                                     |
|}                                                                                           |
|When I send HTTP POST to the relative URL '/post'                                           |
|Then JSON element by JSON path `$.headers.Content-Type` is equal to `"application/json"`    |

Scenario: Verify failure in step "When I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times$stepsToExecute"
When I wait for presence of element by `$.non-existing` for `PT1S` duration retrying 1 times
|step                                                                                          |
|When I send HTTP GET to the relative URL '/status/204'                                        |

Scenario: Verify JSON validator can successfully compare int vs float numbers
Then JSON element from `{"number":0.0}` by JSON path `$.number` is equal to `0`
