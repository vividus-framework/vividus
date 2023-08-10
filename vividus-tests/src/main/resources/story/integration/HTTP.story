Description: Integration tests for HTTP functionality

Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify method DEBUG is supported; accidental space is trimmed in httpMethod enum
When I execute HTTP DEBUG request for resource with URL `http://example.org/`
Then `${response-code}` is equal to `405`

Scenario: Verify handling of plus character in URL query
When I execute HTTP GET request for resource with relative URL `/get?birthDate=<query-parameter-value>`
Then response code is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.url` is equal to `${http-endpoint}get?birthDate=00:00:00%2B02:00`
Then JSON element from `${json-context}` by JSON path `$.args.birthDate` is equal to `00:00:00+02:00`
Examples:
|query-parameter-value|
|00:00:00+02:00       |
|00:00:00%2B02:00     |

Scenario: Verify handling of ampersand character in URL path
When I execute HTTP GET request for resource with relative URL `/anything/path-with-&-ampersand`
Then response code is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.url` is equal to `${http-endpoint}anything/path-with-&-ampersand`

Scenario: Verify handling of ampersand and space characters in URI query parameter
When I execute HTTP GET request for resource with relative URL `/get?key=#{encodeUriQueryParameter(a & b)}`
Then response code is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.url` is equal to `${http-endpoint}get?key=a%20%26%20b`
Then JSON element from `${json-context}` by JSON path `$.args.length()` is equal to `1`
Then JSON element from `${json-context}` by JSON path `$.args.key` is equal to `a & b`

Scenario: Verify handling of encoded special characters in URI query parameter
Given I initialize scenario variable `url` with value `https://user@vividus.dev:vividus.dev/path/segment?a&b=c#fragment`
Given I initialize scenario variable `encodedUrl` with value `#{encodeUri(${url})}`
Then `${encodedUrl}` is equal to `https%3A%2F%2Fuser%40vividus.dev%3Avividus.dev%2Fpath%2Fsegment%3Fa%26b%3Dc%23fragment`
When I execute HTTP GET request for resource with relative URL `/get?key=https:%2F%2Fuser%40vividus.dev:vividus.dev%2Fpath%2Fsegment%3Fa%26b=c%23fragment`
Then response code is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.url` is equal to `${http-endpoint}get?key=https://user@vividus.dev:vividus.dev/path/segment?a%26b=c%23fragment`
Then JSON element from `${json-context}` by JSON path `$.args.length()` is equal to `1`
Then JSON element from `${json-context}` by JSON path `$.args.key` is equal to `${url}`

Scenario: Set HTTP cookies
When I execute HTTP GET request for resource with relative URL `/cookies/set?vividus-cookie=vividus`
When I execute HTTP GET request for resource with relative URL `/cookies`
Then JSON element from `${json-context}` by JSON path `$` is equal to `{"vividus-cookie": "vividus"}`

Scenario: Verify HTTP cookies are cleared
When I execute HTTP GET request for resource with relative URL `/cookies`
Then JSON element from `${json-context}` by JSON path `$` is equal to `{}`

Scenario: Verify step: "When I save value of HTTP cookie with name $cookieName to $scopes variable $variableName"
When I execute HTTP GET request for resource with relative URL `/cookies/set?cookieName=cookieValue`
When I save value of HTTP cookie with name `cookieName` to SCENARIO variable `value`
Then `${value}` is equal to `cookieValue`

Scenario: Verify step: "When I change value of all HTTP cookies with name `$cookieName` to `$newCookieValue`"
When I execute HTTP GET request for resource with relative URL `/cookies/set?name=cookieValue`
When I change value of all HTTP cookies with name `name` to `newCookieValue`
When I save value of HTTP cookie with name `name` to SCENARIO variable `value`
Then `${value}` is equal to `newCookieValue`

Scenario: Validate HTTP retry on service unavailability
Meta:
    @requirementId 214
Given I initialize scenario variable `uuid` with value `#{generate(Internet.uuid)}`
When I execute HTTP GET request for resource with URL `${vividus-test-site-url}/api/teapot?clientId=${uuid}`
Then `${responseCode}` is equal to `200`

Scenario: Validate HTTP methods with missing optional request body
When I execute HTTP <http-method> request for resource with relative URL `/<http-method>`
Then `${responseCode}` is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.json` is equal to `null`
Examples:
|http-method|
|post       |
|put        |
|delete     |

Scenario: Verify step "I add request headers:$headers"
When I set request headers:
|name         |value          |
|Content-Type|application/json|
When I add request headers:
|name    |value|
|Language|en-ru|
When I execute HTTP GET request for resource with relative URL `/get?name=Content`
Then `${response-code}` is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.headers.Content-Type` is equal to `"application/json"`
Then JSON element from `${json-context}` by JSON path `$.headers.Language` is equal to `"en-ru"`


Scenario: Verify step "Given multipart request:$requestParts"
Meta:
    @requirementId 2106
Given I initialize scenario variable `temp-file-content` with value `Your first and last stop for No-Code Test Automation!`
When I create temporary file with name `abc.txt` and content `${temp-file-content}` and put path to scenario variable `temp-file-path`
Given multipart request:
|type  |name      |value            |contentType|fileName       |
|file  |file-key  |/data/file.txt   |           |anotherName.txt|
|file  |file-key2 |${temp-file-path}|text/plain |               |
|string|string-key|string1          |text/plain |               |
|binary|binary-key|raw              |text/plain |raw.txt        |
When I execute HTTP POST request for resource with URL `https://httpbin.org/post`
Then `${responseCode}` is equal to `200`
Then JSON element from `${json-context}` by JSON path `$.files.file-key` is equal to `"#{loadResource(/data/file.txt)}"`
Then JSON element from `${json-context}` by JSON path `$.files.file-key2` is equal to `"${temp-file-content}"`
Then JSON element from `${json-context}` by JSON path `$.form.string-key` is equal to `"string1"`
Then JSON element from `${json-context}` by JSON path `$.files.binary-key` is equal to `"raw"`
Then JSON element from `${json-context}` by JSON path `$.headers.Content-Type` is equal to `"${json-unit.regex}multipart/form-data; charset=ISO-8859-1; boundary=[A-Za-z0-9-_]+"`
Then JSON element from `${json-context}` by JSON path `$.json` is equal to `null`

Scenario: Verify steps "Given request body: $content" (binary content)
Meta:
    @requirementId 1739
Given request body: #{loadBinaryResource(data/image.png)}
When I execute HTTP POST request for resource with relative URL `/post`
Then `${responseCode}` is equal to `200`
When I save JSON element value from `${json-context}` by JSON path `$.data` to scenario variable `data`
Then `${data}` matches `data:application/octet-stream;base64.*`

Scenario: Verify step "Given form data request:$parameters"
Given form data request:
|name     |value      |
|firstName|Alice Marry|
|lastName |Crewe      |
|password |!@3qwer    |
When I execute HTTP POST request for resource with relative URL `/post`
Then JSON element from `${json-context}` by JSON path `$` is equal to `{
  "form": {
    "firstName": [ "Alice Marry" ],
    "lastName": [ "Crewe" ],
    "password": [ "!@3qwer" ]
  },
  "headers": {
     "Content-Type": [ "application/x-www-form-urlencoded; charset=UTF-8" ]
  }
}`ignoring extra fields

Scenario: Verify variable with binary data resolution
Given form data request:
|name     |value      |
|firstName|Alice Marry|
|lastName |Crewe      |
|password |!@3qwer    |
When I execute HTTP POST request for resource with relative URL `/post`
Then JSON element from `${response-as-bytes}` by JSON path `$` is equal to `{
  "form": {
    "firstName": [ "Alice Marry" ],
    "lastName": [ "Crewe" ],
    "password": [ "!@3qwer" ]
  },
  "headers": {
     "Content-Type": [ "application/x-www-form-urlencoded; charset=UTF-8" ]
  }
}`ignoring extra fields
