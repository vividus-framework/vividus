Description: Integration tests for REST API steps

Meta:
    @epic vividus-plugin-rest-api

Lifecycle:
Before:
Scope: STORY
Given I initialize story variable `request-body` with value `{
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
Then response code is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.json` is equal to `${request-body}`
Then JSON element from `${json-context}` by JSON path `$.headers.Content-Type` is equal to `"application/json"`

Scenario: Verify steps "Given request body: $content" and "When I execute HTTP $httpMethod request for resource with relative URL `$relativeURL`"
Given request body: ${request-body}
When I execute HTTP PUT request for resource with relative URL `/put`

Scenario: Verify steps "Given request body: $content" and "When I execute HTTP $httpMethod request for resource with URL `$url`"
Given request body: ${request-body}
When I execute HTTP DELETE request for resource with URL `${http-endpoint}delete`

Scenario: Verify steps "When I execute HTTP $httpMethod request for resource with URL `$url`"
Given request body: ${request-body}
When I execute HTTP POST request for resource with URL `${http-endpoint}post`

Scenario: Verify step "Then HTTP resources are valid:$resources"
Then HTTP resources are valid:
|url                                     |
|${vividus-test-site-url}/img/vividus.png|
|${vividus-test-site-url}/links.html     |
