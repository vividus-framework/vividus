Description: Integration tests for JsonResponseValidationSteps class (base website http://jsonpath.herokuapp.com/)

Meta:
    @epic vividus-plugin-rest-api,vividus-plugin-json

Lifecycle:
Before:
Scope: STORY
When I execute HTTP GET request for resource with URL `http://jsonpath.herokuapp.com/json/goessner.json`
Then `${responseCode}` matches `20\d`
Examples:
|URL                                             |jsonPath             |booksNumber|
|http://jsonpath.herokuapp.com/json/goessner.json|$.store.book.length()|4          |


Scenario: Verify step 'When I save JSON element from `$json` by JSON path `$jsonPath` to $scopes variable `$variableName`'
When I save JSON element from `${response}` by JSON path `<jsonPath>` to scenario variable `numberOfBooks`
Then `${numberOfBooks}` is equal to `<booksNumber>`

Scenario: Verify step 'Then number of JSON elements by JSON path `$jsonPath` is $comparisonRule $elementsNumber'
Then number of JSON elements by JSON path `$.store.book` is greater than 0

Scenario: Verify step 'When I set number of elements found by JSON path `$jsonPath` to $scopes variable `$variableName`'
When I set number of elements found by JSON path `$.store.book` to scenario variable `bookCount`
Then `${bookCount}` is greater than `0`

Scenario: Verify step 'When I find $comparisonRule `$elementsNumber` JSON elements by `$jsonPath` and for each element do$stepsToExecute'
When I find greater than `1` JSON elements by `$.store.book` and for each element do
|step                                                       |
|Then number of JSON elements by JSON path `$.author` is = 1|

Scenario: Verify step "When I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times$stepsToExecute"
When I wait for presence of element by `$.json.iteration3` for `PT15S` duration retrying 3 times
|step                                                                                     |
|Given I initialize scenario variable `iteration` with value `#{eval(${iteration:0} + 1)}`|
|When I set request headers:                                                              |
|{headerSeparator=!,valueSeparator=!}                                                     |
|!name         !value            !                                                        |
|!Content-Type !application/json !                                                        |
|Given request body: {                                                                    |
|  "iteration${iteration}": ${iteration}                                                  |
|}                                                                                        |
|When I execute HTTP POST request for resource with relative URL `/post`                  |
|Then JSON element by JSON path `$.headers.Content-Type` is equal to `"application/json"` |

Scenario: Verify failure in step "When I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times$stepsToExecute"
When I wait for presence of element by `$.non-existing` for `PT1S` duration retrying 1 times
|step                                                                        |
|When I execute HTTP GET request for resource with relative URL `status/204` |
