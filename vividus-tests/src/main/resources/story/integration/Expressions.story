Description: Integration tests for various Vividus expressions

Meta:
    @epic vividus-bdd-engine
    @feature expressions

Scenario: [Deprecated] Verify date generation and format
When I initialize the scenario variable `currentDate` with value `#{P}`
Then `#{formatDateTo(${currentDate}, yyyy-MM-dd, yyyy)}` is equal to `#{P(yyyy)}`

Scenario: Verify date generation and format
Then `#{formatDateTo(#{generateDate(P)}, yyyy-MM-dd, yyyy)}` is equal to `#{generateDate(P, yyyy)}`

Scenario: Verify epoch generation and conversion
When I initialize the SCENARIO variable `date` with value `#{generateDate(P, yyyy-MM-dd'T'HH:mm:ss)}`
When I initialize the SCENARIO variable `epoch` with value `#{toEpochSecond(${date})}`
Then `${date}` is equal to `#{fromEpochSecond(${epoch})}`

Scenario: Verify anyOf expression
Then `#{anyOf(1, 2\,3,3)}` matches `1|2,3|3`

Scenario: Verify diffDate with formatting
Then `777` is = `#{diffDate(2019-01-01T12:00:00.223Z,yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:01.000Z,yyyy-MM-dd'T'HH:mm:ss.SSSVV, milliS)}`

Scenario: Verify eval expression
Then `#{<expression>}` is = `<expected>`

Examples:
|expected |expression                      |
|null     |eval(null)                      |
|28       |eval(16 + 2 * 6)                |
|108      |eval((16 + 2) * 6)              |
|-6       |eval(100 / 5 - 16 * 2 + 6)      |
|true     |eval(`string\n1` == `string\n1`)|
|false    |eval(`string\n1` == `string1`)  |


Scenario: Verify eval has an access to a variable context
Meta:
    @requirementId 696
When I initialize the story variable `someVar` with value `<contextVar>`
Then `#{<expression>}` is = `<expected>`

Examples:
|expected|expression                          |contextVar|
|2       |eval(${someVar} + 1)                |1         |
|11      |eval(someVar + 1)                   |1         |
|11      |eval(key = 1; someVar + key)        |1         |
|2       |eval(someVar = 1; someVar + someVar)|1         |
|VALUE   |eval(someVar.toUpperCase())         |value     |
|123     |eval(someVar.replaceAll('\D', ''))  |va1lu2e3  |
