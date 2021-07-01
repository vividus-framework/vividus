Description: Integration tests for various Vividus expressions

Meta:
    @epic vividus-bdd-engine
    @feature expressions

Scenario: [Deprecated] Verify date generation and format
When I initialize the scenario variable `currentDate` with value `#{generateDate(P)}`
Then `#{formatDateTo(${currentDate}, yyyy-MM-dd, yyyy)}` is equal to `#{generateDate(P, yyyy)}`

Scenario: Verify date generation and format
Then `#{formatDateTo(#{generateDate(P)}, yyyy-MM-dd, yyyy)}` is equal to `#{generateDate(P, yyyy)}`

Scenario: Verify epoch generation and conversion
When I initialize the SCENARIO variable `date` with value `#{generateDate(P, yyyy-MM-dd'T'HH:mm:ss)}`
When I initialize the SCENARIO variable `epoch` with value `#{toEpochSecond(${date})}`
Then `${date}` is equal to `#{fromEpochSecond(${epoch})}`

Scenario: Verify epoch generation with timezone
When I initialize the SCENARIO variable `epoch` with value `#{toEpochSecond(2020-12-11T18:43:05+05:30)}`
Then `${epoch}` is equal to `1607692385`

Scenario: Verify anyOf expression
Then `#{anyOf(1, 2\,3,3)}` matches `1|2,3|3`

Scenario: Verify diffDate with formatting
Then `777` is = `#{diffDate(2019-01-01T12:00:00.223Z,yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:01.000Z,yyyy-MM-dd'T'HH:mm:ss.SSSVV, milliS)}`

Scenario: Verify eval expression
Then `#{<expression>}` is = `<expected>`
Examples:
|expression                                                        |expected          |
|eval(null)                                                        |null              |
|eval(16 + 2 * 6)                                                  |28                |
|eval(math:abs(-10))                                               |10                |
|eval(stringUtils:substringAfterLast('namescpaces are %here', '%'))|here              |
|eval((16 + 2) * 6)                                                |108               |
|eval(100 / 5 - 16 * 2 + 6)                                        |-6                |
|eval(`string\n1` == `string\n1`)                                  |true              |
|eval(`string\n1` == `string1`)                                    |false             |
|eval(wordUtils:capitalize('i am FINE'))                           |I Am FINE         |
|eval(wordUtils:uncapitalize('I Am FINE'))                         |i am fINE         |
|eval(wordUtils:swapCase('The dog has a BONE'))                    |tHE DOG HAS A bone|
|eval(wordUtils:initials('Fus Ro Dah'))                            |FRD               |


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

Scenario: Verify groovy expression
When I initialize Scenario variable `listOfMaps` with values:
|key|
|2  |
|1  |
|3  |
Then `1-2-3` is = `#{evalGroovy(return listOfMaps.collect{it['key']}.sort().join('-'))}`

Scenario: Verify 'trim' expression
Then `#{trim( A B C )}` is equal to `A B C`
Then `#{trim(
A B C
)}` is equal to `A B C`

Scenario: Verify 'randomInt' expression with another string
When I initialize the scenario variable `var` with value `#{randomInt(42, 42)} and 24`
Then `${var}` is equal to `42 and 24`

Scenario: Verify 'toBase64Gzip' expression
Meta:
    @requirementId 1337
Then `#{toBase64Gzip(vividus)}` is equal to `H4sIAAAAAAAAACvLLMtMKS0GANIHCdkHAAAA`

Scenario: Use expressions in template
Meta:
    @requirementId 1545
Given I initialize the SCENARIO variable `data` using template `data/expressions.ftl` with parameters:
|param|
|value|
Then `${data}` is equal to `Vividus Expressions
TG9yZCBEYWdvbiB3aWxsIHdlbGNvbWUgeW91ciBzb3VsIGluIE9ibGl2aW9uIQ==
Mg==
1986-04-26T01:23:40+04:00
`

Scenario: Verify capitlizing/uncapitalizing expressions
Then `#{<expression>}` is = `<expected>`
Examples:
|expression                   |expected  |
|capitalize(aBc)              |ABc       |
|capitalizeFirstWord(aBc)     |ABc       |
|capitalizeWords(aBc dEf)     |ABc DEf   |
|capitalizeWordsFully(aBc dEf)|Abc Def   |
|uncapitalize(ABc)            |aBc       |
|uncapitalizeFirstWord(ABc)   |aBc       |
|uncapitalizeWords(ABc DEf)   |aBc dEf   |
