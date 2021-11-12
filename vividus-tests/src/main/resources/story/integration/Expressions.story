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
Then `#{toBase64Gzip(vividus)}` is equal to `H4sIAAAAAAAA/yvLLMtMKS0GANIHCdkHAAAA`

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

Scenario: Verify hash expressions
Meta:
    @requirementId 1647
Then `#{<expression>}` is = `<expected>`
Examples:
|expression                                  |expected                                                                                                                         |
|calculateHash(SHA-1, some_mail@gmail.com)   |83a7c7215b3d0052b6b9b9154ff0608a6890fd45                                                                                         |
|calculateHash(Sha-256, some_mail@gmail.com) |81f98afdd1738a76e06649c1d9c27d15eaa7cd57e408b0f32e831728c80bd124                                                                 |
|calculateHash(MD2, some_mail@gmail.com)     |556c1d6a0abd28ed40d364fe114518c4                                                                                                 |
|calculateHash(Md5, some_mail@gmail.com)     |ade052992d2105f5aae8f2a0893318ea                                                                                                 |
|calculateHash(Sha384, some_mail@gmail.com)  |df0e51dd5bbb2aee58ace845c655006c74803179261971403c3b7430abeed55cb3097a6519dbf2138c09a7b893d9c08b                                 |
|calculateHash(sha-512, some_mail@gmail.com) |46c9fc926b32077b0f54ac872f8118ecd50824a1c2fa8f538d1a25eb341e3cc90dbe98a9e6b5c293becfcd965ed41bd950b5d06d0a30d774cec77a0a49aa2141 |

Scenario: Verify file hash expressions
Meta:
    @requirementId 1647
Then `#{<expression>}` is = `<expected>`
Examples:
|expression                                |expected                                                                                                                         |
|calculateFileHash(SHA-1, data/file.txt)   |625558f5355e182fd4684f02e459f64ac9341f19                                                                                         |
|calculateFileHash(Sha-256, data/file.txt) |6fb11c13c1a5a71d3f2b0075562e4873c6823a8508d44a2fd0113fd6d307cd7f                                                                 |
|calculateFileHash(MD2, data/file.txt)     |fa097512218e0f44febc5f9fadbe2bd6                                                                                                 |
|calculateFileHash(Md5, data/file.txt)     |969015df4bea10782823bedbf48055e0                                                                                                 |
|calculateFileHash(Sha384, data/file.txt)  |8fa517c0df035d579fae2ac4fd2e87de50a761b6249875607dbd01c64469b51bcc276ed9a3313031bba649d8577f5058                                 |
|calculateFileHash(sha-512, data/file.txt) |ade64012f60b0620f285852f32a18e2e729c7a2a9810709d1786b344156525fef0a046d4f79104e193e98c6c80f6c744b97cb8410e55a5a8ef7f60623c583757 |
