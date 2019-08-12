Description: Integration tests for JsonResponseValidationSteps class (base website http://jsonpath.herokuapp.com/)

Meta:
    @group vividus-plugin-rest-api

Lifecycle:
Before:
Scope: STORY
When I issue a HTTP GET request for a resource with the URL 'http://jsonpath.herokuapp.com/json/goessner.json'
Then `${responseCode}` matches `20\d`

Scenario: Verify composite step 'When I save a JSON element from response by JSON path '$jsonPath' to $scopes variable '$variableName''
When I save a JSON element from response by JSON path '$.store.book.length()' to scenario variable 'numberOfBooks'
Then `${numberOfBooks}` is equal to `4`
