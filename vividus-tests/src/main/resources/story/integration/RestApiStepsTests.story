Description: Integration tests for REST API steps

Meta:
    @epic vividus-plugin-rest-api

Lifecycle:
Before:
Scope: STORY
When I initialize the story variable `request-body` with value `{
    "string": "<, ', :, \"\"  symbols codes are &lt;, &#39;, &#58;, &#34;",
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
Then JSON element by JSON path `$.json` is equal to `${request-body}`
Then JSON element by JSON path `$.headers.Content-Type` is equal to `"application/json"`

Scenario: Verify steps "Given request body: $content" and "When I send HTTP $httpMethod to the relative URL '$relativeURL'"
Given request body: ${request-body}
When I send HTTP PUT to the relative URL '/put'

Scenario: Verify step "When I send HTTP $httpMethod to the relative URL '$relativeURL' with content: '$content'"
When I send HTTP POST to the relative URL '/post' with content: '${request-body}'

Scenario: Verify steps "Given request body: $content" and "When I issue a HTTP $httpMethod request for a resource with the URL '$url'"
Given request body: ${request-body}
When I issue a HTTP DELETE request for a resource with the URL 'https://httpbin.org/delete'

Scenario: Verify steps "When I execute HTTP $httpMethod request for resource with URL `$url`"
Given request body: ${request-body}
When I execute HTTP POST request for resource with URL `https://httpbin.org/post`

Scenario: Verify step "Then the connection is secured using $securityProtocol protocol"
Given request body: ${request-body}
When I send HTTP DELETE to the relative URL '/delete'
Then the connection is secured using TLSv1.2 protocol

Scenario: Verify step "Then sHTTP resources are valid:$resources"
Then HTTP resources are valid:
|url                                     |
|${vividus-test-site-url}/img/vividus.png|
