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

!-- Composites validation

Scenario: Verify composite step 'When I wait for presence of the element by JSON path '$jsonPath' in HTTP GET response from '$resourceUrl' for '$duration' duration'
When I wait for presence of the element by JSON path '<jsonPath>' in HTTP GET response from '<URL>' for 'PT30S' duration
When I save a JSON element from response by JSON path '<jsonPath>' to scenario variable 'numberOfBooks'
Then `${numberOfBooks}` is equal to `<booksNumber>`
