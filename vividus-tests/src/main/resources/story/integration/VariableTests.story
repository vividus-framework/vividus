Description: Integration tests for variables

Meta:
    @epic vividus-engine
    @feature variables

Scenario: Verify default variables
Then `1` is = `${key:1}`
Then `2` is = `${list[0]:2}`
Then `3` is = `${map.key:3}`
Then `4` is = `${list[0].key:4}`
Then `:I:am:here:` is = `${not-exists::I:am:here:}`

Scenario: Verify batch scope has higher priority than global scope
Then `${scope-priority-check}` is equal to `should be batch`

Scenario: Validate variable initialization
Given I initialize scenario variable `java` with value `NIO`
Then `${java}` is equal to `NIO`

Scenario: Verify step for saving examples table to variable as list of maps
When I initialize SCENARIO variable `testMap` with values:
|key1  |key2  |
|value1|value2|
Then `value1` is = `${testMap[0].key1}`
Then `value2` is = `${testMap[0].key2}`
When I initialize SCENARIO variable `testEmptyMap` with values:
Then `` is = `${testEmptyMap[0].key:}`

Scenario: Verify ability to use variables with names containing special characters
!-- The following checks verify handling of non-initialized BDD variables
Then `${vAr}` is equal to `${vAr}`
Then `${v.ar}` is equal to `${v.ar}`
Then `${var[0]}` is equal to `${var[0]}`
Then `${v[0].ar}` is equal to `${v[0].ar}`
Then `${v.ar[0]}` is equal to `${v.ar[0]}`
Then `${v:ar}` is equal to `${v:ar}`
Then `${}` is equal to `${}`
Then `${:default}` is equal to `default`
Then `${a.b:NULL}` is equal to `NULL`
Then `${foo:${bar}}` is equal to `${foo:${bar}}`
Then `${baz#{eval(0 + 2)}:${foo#{eval(0 + 1)}}}` is equal to `${baz2:${foo1}}`

Given I initialize scenario variable `vAr` with value `vAl`
Given I initialize scenario variable `foo` with value `foo`
Given I initialize scenario variable `foo1` with value `foo1`
Given I initialize scenario variable `v.ar` with value `v.al`
Given I initialize scenario variable `var[0]` with value `val[0]`
Given I initialize scenario variable `v[0].ar` with value `v[0].al`
Given I initialize scenario variable `v.ar[0]` with value `v.al[0]`
Given I initialize scenario variable `v:ar` with value `v:al`
Given I initialize scenario variable `` with value `val`
Given I initialize scenario variable `a.b` with value `a.b-value`
Then `${vAr}`                      is equal to `vAl`
Then `${v.ar}`                     is equal to `v.al`
Then `${var[0]}`                   is equal to `val[0]`
Then `${v[0].ar}`                  is equal to `v[0].al`
Then `${v.ar[0]}`                  is equal to `v.al[0]`
Then `${v:ar}`                     is equal to `v:al`
Then `${}`                         is equal to `val`
Then `${a.b:NULL}`                 is equal to `a.b-value`
Then `${a.b}`                      is equal to `a.b-value`
Then `${foo:${bar}}`               is equal to `foo`
Then `${foo}:${foo}`               is equal to `foo:foo`
Then `${baz:${foo}}`               is equal to `foo`
Then `${baz:a${foo}z}`             is equal to `afooz`
Then `${baz:${foo#{eval(0 + 1)}}}` is equal to `foo1`
Then `${not-existing:}`            is equal to ``

Scenario: Verify that expression can be used as a part of variable name and vice versa
Meta:
    @issueId 691
Given I initialize scenario variable `var3` with value `4`
Given I initialize scenario variable `var5` with value `5`
Then `${var#{eval(${var#{eval(<iterator> + 1)}} + 1)}}` is = `5`
Examples:
{transformer=ITERATING, startInclusive=2, endInclusive=2}

Scenario: Verify that circular variable references are resolved without StackOverflowError
Meta:
    @issueId 691
Given I initialize scenario variable `value` with value `#{removeWrappingDoubleQuotes(${value})}`
Then `${value}` is equal to `${value}`
Then `before-${value}-after` is equal to `before-${value}-after`

Scenario: Verify that variables are resolved in examples table used as a parameter for a step
Meta:
    @issueId 692
Given I initialize scenario variable `var` with value `val`
Given I initialize scenario variable `template-result` using template `/data/simple-template.ftl` with parameters:
/data/table-with-scenario-level-variables.table
Then `${template-result}` is equal to `passed: variable is resolved`

Scenario: Verify that variable can be used as an input for the step generating data from Freemarker template
When I initialize scenario variable `table` with values:
|country     |capital   |
|Belarus     |Minsk     |
|Netherlands |Amsterdam |
Given I initialize scenario variable `countries-json` using template `/data/complex-template.ftl` with parameters:
${table}
Then JSON element from `${countries-json}` by JSON path `$` is equal to `
[
    {
        "country": "Belarus",
        "capital": "Minsk"
    },
    {
        "country": "Netherlands",
        "capital": "Amsterdam"
    }
]`

Scenario: Verify that variables of different nesting step-levels are cleaned up only for the current level
Meta:
    @issueId 763
When I execute steps while counter is <= `1` with increment `1` starting from `1`:
|step                                                                          |
|Given I initialize scenario variable `test1` with value `${iterationVariable}`|
|When the condition 'true' is true I do                                        |
|{headerSeparator=!,valueSeparator=!}                                          |
|!step                                                          !              |
|!Given I initialize scenario variable `value` with value `test`!              |
|Given I initialize scenario variable `test2` with value `${iterationVariable}`|
|Then `${test1}` is equal to `${test2}`                                        |

Scenario: Verify that variables of different nesting step-levels don't affect each other
Meta:
    @issueId 763
When I execute steps while counter is <= `1` with increment `1` starting from `1`:
|step                                                                            |
|Given I initialize scenario variable `test1` with value `${iterationVariable}`  |
|When the condition 'true' is true I do                                          |
|{headerSeparator=!,valueSeparator=!}                                            |
|!step                                                                          !|
|!Then `${test1}` is equal to `1`                                               !|
|!Given I initialize scenario variable `iterationVariable` with value `42`      !|
|!Given I initialize scenario variable `test2` with value `${iterationVariable}`!|
|Then `${test2}` is equal to `1`                                                 |
Then `${iterationVariable}` is equal to `42`

Scenario: Pass ExamplesTable as variable to composite step accepting ExamplesTable
Meta:
    @issueId 1136
When I initialize scenario variable `table` with values:
|column |
|value  |
When I run composite step with table:${table}
Then `${table[0].column}` is equal to `value`
Then `${table-from-composite-step[0].column}` is equal to `value`

Scenario: Use variables as part of names of another variables
Meta:
    @issueId 1181
Given I initialize scenario variable `variable` with value `Variable`
Given I initialize scenario variable `Variable` with value `nested`
Given I initialize scenario variable `compositeVariable` with value `expected`
Given I initialize scenario variable `expected` with value `nested-expected`
Given I initialize scenario variable `complex` with value `[{ids=2}, {ids=0}, {ids=7}, {ids=7}]`
Then `${composite${variable}}`                                            is equal to `expected`
Then `${${variable}}`                                                     is equal to `nested`
Then `${${variable}} and ${${variable}}`                                  is equal to `nested and nested`
Then `${composite:${composite${variable}}}`                               is equal to `expected`
Then `#{eval(${non-existing:0} + 1)}`                                     is equal to `1`
Then `#{replaceAllByRegExp("""\},\s\{ids=""", """""", """${complex}""")}` is equal to `[{ids=2077}]`

Scenario: Use variables in template
Meta:
    @requirementId 1545
Given I initialize scenario variable `scenarioVar` with value `1`
Given I initialize scenario variable `overrideVar` with value `-1`
Given I initialize story variable `storyVar` with value `2`
Given I initialize scenario variable `numbers` using template `data/resolve-variables.ftl` with parameters:
|exampleParam|overrideVar|
|3           |4          |
Then `#{trim(${numbers})}` is equal to `12345`

Scenario: Use environment variable as Vividus variable
Then `${java}` is equal to `${JAVA_HOME}`

Scenario: Compare variable with int value
Then `${property-with-int-value}` is equal to `4`

Scenario: The multiline value can be matched using regex
Meta:
    @issueId 1967
Then `
A
B
C
` matches `.*B.*`

Scenario: Verify null values in variables
Meta:
    @issueId 2378
Given I initialize scenario variable `nullVar` with value `#{null}`
When I initialize SCENARIO variable `varsMap` with values:
|key     |
|#{null} |
Then `${nullVar}` is equal to `#{null}`
Then `${varsMap[0].key}` is equal to `#{null}`
