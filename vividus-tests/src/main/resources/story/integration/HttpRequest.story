Description: Integration tests for ApiSteps functionality

Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify method DEBUG is supported
When I issue a HTTP DEBUG request for a resource with the URL 'http://example.org/'
Then `${responseCode}` is equal to `405`
