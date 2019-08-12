Description: Tests to demonstrate working with JSON Web Tokens (JWTs)

Meta:
    @group JWT

Scenario: Decode/encode JWTs, validate extracked jsons
When I initialize the scenario variable `JWT` with value `#{loadResource(JWT.txt)}`
When I initialize the scenario variable `header` with value `#{decodeFromBase64(#{eval(`${JWT}`.replaceFirst("([^.]+).*", "$1"))})}`
When I initialize the scenario variable `payload` with value `#{decodeFromBase64(#{eval(`${JWT}`.replaceFirst(".*(?<=\.)(.*?)(?=\.).*", "$1"))})}`
When I initialize the scenario variable `encodedSignature` with value `#{eval(`${JWT}`.replaceFirst(".*(?<=\.)([^.]+)$", "$1"))}`
When I save a JSON element from '${header}' by JSON path 'alg' to scenario variable 'alg'
Then `#{removeWrappingDoubleQuotes(${alg})}` is = `HS256`
Then number of JSON elements from `${header}` by JSON path `typ` is = 1
Then a JSON element from '${payload}' by the JSON path 'sub' is equal to '"1234567890"'TREATING_NULL_AS_ABSENT
Then number of JSON elements from `${payload}` by JSON path `name` is = 1
Then `#{encodeToBase64(${header})}.#{eval(`#{encodeToBase64(${payload})}`.replaceFirst("==", ""))}.${encodedSignature}` is = `${JWT}`
