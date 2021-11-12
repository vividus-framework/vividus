Description: Integration tests for HTTP functionality

Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify method DEBUG is supported; accidental space is trimmed in httpMethod enum
When I issue a HTTP DEBUG  request for a resource with the URL 'http://example.org/'
Then `${response-code}` is equal to `405`

Scenario: Verify handling of plus character in URL query
When I send HTTP GET to the relative URL '/get?birthDate=<query-parameter-value>'
Then the response code is equal to '200'
Then JSON element by JSON path `$.url` is equal to `${http-endpoint}get?birthDate=00:00:00%2B02:00`
Then JSON element by JSON path `$.args.birthDate` is equal to `00:00:00+02:00`
Examples:
|query-parameter-value|
|00:00:00+02:00       |
|00:00:00%2B02:00     |

Scenario: Verify handling of ampersand character in URL path
When I send HTTP GET to the relative URL '/anything/path-with-&-ampersand'
Then the response code is equal to '200'
Then JSON element by JSON path `$.url` is equal to `${http-endpoint}anything/path-with-&-ampersand`

Scenario: Verify handling of ampersand and space characters in URI query parameter
When I send HTTP GET to the relative URL '/get?key=#{encodeUriQueryParameter(a & b)}'
Then the response code is equal to '200'
Then JSON element by JSON path `$.url` is equal to `${http-endpoint}get?key=a %26 b`
Then JSON element by JSON path `$.args.length()` is equal to `1`
Then JSON element by JSON path `$.args.key` is equal to `a & b`

Scenario: Set HTTP cookies
When I send HTTP GET to the relative URL '/cookies/set?vividus-cookie=vividus'
When I send HTTP GET to the relative URL '/cookies'
Then JSON element by JSON path `$.cookies` is equal to `{"vividus-cookie": "vividus"}`

Scenario: Verify HTTP cookies are cleared
When I send HTTP GET to the relative URL '/cookies'
Then JSON element by JSON path `$.cookies` is equal to `{}`

Scenario: I wait for response code $responseCode for $duration duration retrying $retryTimes times $stepsToExecute
!-- Warm up Vividus test site
When I issue a HTTP GET request for a resource with the URL '${vividus-test-site-url}/'
When I initialize the scenario variable `uuid` with value `#{generate(Internet.uuid)}`
When I wait for response code `200` for `PT10S` duration retrying 10 times
|step                                                                                                                        |
|When I issue a HTTP GET request for a resource with the URL '${vividus-test-site-url}/api/delayed-response?clientId=${uuid}'|
Then `${responseCode}` is equal to `200`

Scenario: Validate HTTP retry on service unavailability
Meta:
    @requirementId 214
When I initialize the scenario variable `uuid` with value `#{generate(Internet.uuid)}`
When I issue a HTTP GET request for a resource with the URL '${vividus-test-site-url}/api/teapot?clientId=${uuid}'
Then `${responseCode}` is equal to `200`

Scenario: Validate HTTP methods with missing optional request body and zero content-length
When I send HTTP <http-method> to the relative URL '/<http-method>'
Then `${response-code}` is equal to `200`
Then JSON element by JSON path `$.json` is equal to `null`
Then JSON element by JSON path `$.headers.Content-Length` is equal to `"0"`
Examples:
|http-method|
|post       |

Scenario: Validate HTTP methods with missing optional request body
When I send HTTP <http-method> to the relative URL '/<http-method>'
Then `${responseCode}` is equal to `200`
Then JSON element by JSON path `$.json` is equal to `null`
Examples:
|http-method|
|put        |
|delete     |

Scenario: Verify step "I add request headers:$headers"
When I set request headers:
|name         |value          |
|Content-Type|application/json|
When I add request headers:
|name    |value|
|Language|en-ru|
When I send HTTP GET to the relative URL '/get?name=Content'
Then `${response-code}` is equal to `200`
Then JSON element by JSON path `$.headers.Content-Type` is equal to `"application/json"`
Then JSON element by JSON path `$.headers.Language` is equal to `"en-ru"`


Scenario: Verify step "Given multipart request:$requestParts"
Meta:
    @requirementId 2106
When I initialize the scenario variable `temp-file-content` with value `Your first and last stop for No-Code Test Automation!`
When I create temporary file with name `abc.txt` and content `${temp-file-content}` and put path to scenario variable `temp-file-path`
Given multipart request:
|type  |name      |value            |contentType|fileName       |
|file  |file-key  |/data/file.txt   |           |anotherName.txt|
|file  |file-key2 |${temp-file-path}|text/plain |               |
|string|string-key|string1          |text/plain |               |
|binary|binary-key|raw              |text/plain |raw.txt        |
When I send HTTP POST to the relative URL '/post'
Then `${responseCode}` is equal to `200`
Then JSON element by JSON path `$.files.file-key` is equal to `"#{loadResource(/data/file.txt)}"`
Then JSON element by JSON path `$.files.file-key2` is equal to `"${temp-file-content}"`
Then JSON element by JSON path `$.form.string-key` is equal to `"string1"`
Then JSON element by JSON path `$.files.binary-key` is equal to `"raw"`
Then JSON element by JSON path `$.headers.Content-Type` is equal to `"${json-unit.regex}multipart/form-data; boundary=[A-Za-z0-9-_]+"`
Then JSON element by JSON path `$.json` is equal to `null`

Scenario: Verify steps "Given request body: $content" (binary content)
Meta:
    @requirementId 1739
Given request body: #{loadBinaryResource(data/image.png)}
When I send HTTP POST to the relative URL '/post'
Then `${responseCode}` is equal to `200`
