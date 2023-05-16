Description: Tests to demonstrate working with JSON Web Tokens (JWTs)

Meta:
    @epic vividus-examples
    @feature JWT

Scenario: Decode/encode/generate JWTs, validate extracked jsons
Given I initialize scenario variable `header` with value `{"typ":"JWT","alg":"HS256"}`
Given I initialize scenario variable `payload` with value `{"sub":"1234567890","name":"John Doe","admin":true,"jti":"c6859320-9fb3-4784-8c2f-1ab37044acfc","iat":#{toEpochSecond(#{generateDate(P, yyyy-MM-dd'T'HH:mm:ss)})},"exp":#{toEpochSecond(#{generateDate(P1D, yyyy-MM-dd'T'HH:mm:ss)})}}`
When I generate JWT with header `${header}` and payload `${payload}` signed with key `secretKey` using HS256 algorithm and save result to scenario variable `JWT`
Given I initialize scenario variable `decodedHeader` with value `#{decodeFromBase64(#{eval(`${JWT}`.replaceFirst("([^.]+).*", "$1"))})}`
Given I initialize scenario variable `decodedPayload` with value `#{decodeFromBase64(#{eval(`${JWT}`.replaceFirst(".*(?<=\.)(.*?)(?=\.).*", "$1"))})}`
Given I initialize scenario variable `encodedSignature` with value `#{eval(`${JWT}`.replaceFirst(".*(?<=\.)([^.]+)$", "$1"))}`
When I save JSON element from `${decodedHeader}` by JSON path `alg` to scenario variable `alg`
Then `#{removeWrappingDoubleQuotes(${alg})}` is = `HS256`
Then number of JSON elements from `${decodedHeader}` by JSON path `typ` is = 1
Then JSON element from `${decodedPayload}` by JSON path `sub` is equal to `"1234567890"`TREATING_NULL_AS_ABSENT
Then number of JSON elements from `${decodedPayload}` by JSON path `name` is = 1
Then `#{encodeToBase64(${decodedHeader})}.#{eval(`#{encodeToBase64(${decodedPayload})}`.replaceFirst("==", ""))}.${encodedSignature}` is = `${JWT}`
When I generate JWT with header `${decodedHeader}` and payload `${decodedPayload}` signed with key `secretKey` using HS256 algorithm and save result to scenario variable `generatedFromDecodedDataJWT`
Then `${generatedFromDecodedDataJWT}` is = `${JWT}`
