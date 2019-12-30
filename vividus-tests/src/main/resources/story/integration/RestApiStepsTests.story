Description: Integration tests for REST API steps

Meta:
    @group vividus-plugin-rest-api

Lifecycle:
Before:
Scope: STORY
When I initialize the story variable `request-body` with value `{
    "string": "string",
    "number": 42,
    "boolean": true
}`
Scope: SCENARIO
When I set request headers:
|name         |value          |
|Content-Type|application/json|
After:
Scope: SCENARIO
Then the response code is equal to '200'
Then a JSON element by the JSON path '$.json' is equal to '${request-body}'
Then a JSON element by the JSON path '$.headers.Content-Type' is equal to '"application/json"'

Scenario: Verify steps "Given request body: $content" and "When I send HTTP $httpMethod to the relative URL '$relativeURL'"
Given request body: ${request-body}
When I send HTTP PUT to the relative URL '/put'

Scenario: Verify step "When I send HTTP $httpMethod to the relative URL '$relativeURL' with content: '$content'"
When I send HTTP POST to the relative URL '/post' with content: '${request-body}'

Scenario: Verify steps "Given request body: $content" and "When I issue a HTTP $httpMethod request for a resource with the URL '$url'"
Given request body: ${request-body}
When I issue a HTTP DELETE request for a resource with the URL 'https://httpbin.org/delete'

Scenario: Verify step "Then the connection is secured using $securityProtocol protocol"
Given request body: ${request-body}
When I send HTTP DELETE to the relative URL '/delete'
Then the connection is secured using TLSv1.2 protocol
