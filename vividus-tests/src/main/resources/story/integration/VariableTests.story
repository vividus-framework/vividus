Description: Integration tests for variables

Meta:
    @epic vividus-bdd-engine
    @feature variables

Scenario: Verify default variables
Then `1` is = `${key:1}`
Then `2` is = `${list[0]:2}`
Then `3` is = `${map.key:3}`
Then `4` is = `${list[0].key:4}`

Scenario: Verify batch scope has higher priority than global scope
Then `${scope-priority-check}` is equal to `should be batch`

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

When I initialize the scenario variable `vAr` with value `vAl`
When I initialize the scenario variable `v.ar` with value `v.al`
When I initialize the scenario variable `var[0]` with value `val[0]`
When I initialize the scenario variable `v[0].ar` with value `v[0].al`
When I initialize the scenario variable `v.ar[0]` with value `v.al[0]`
When I initialize the scenario variable `v:ar` with value `v:al`
When I initialize the scenario variable `` with value `val`
When I initialize the scenario variable `a.b` with value `a.b-value`
Then `${vAr}` is equal to `vAl`
Then `${v.ar}` is equal to `v.al`
Then `${var[0]}` is equal to `val[0]`
Then `${v[0].ar}` is equal to `v[0].al`
Then `${v.ar[0]}` is equal to `v.al[0]`
Then `${v:ar}` is equal to `v:al`
Then `${}` is equal to `val`
Then `${a.b:NULL}` is equal to `a.b-value`
Then `${a.b}` is equal to `a.b-value`

Scenario: Verify that expression can be used as a part of variable name and vice versa
Meta:
    @issueId 691
When I initialize the scenario variable `var1` with value `2`
When I initialize the scenario variable `var3` with value `3`
Then `${var#{eval(${var#{eval(<iterator> + 1)}} + 1)}}` is = `3`
Examples:
{transformer=ITERATING, limit=1}

Scenario: Verify that circular variable references are resolved without StackOverflowError
Meta:
    @issueId 691
When I initialize the scenario variable `value` with value `#{removeWrappingDoubleQuotes(${value})}`
Then `${value}` is equal to `${value}`
Then `before-${value}-after` is equal to `before-${value}-after`

Scenario: Verify that variables are resolved in examples table used as a parameter for a step
Meta:
    @issueId 692
When I initialize the scenario variable `var` with value `val`
Given I initialize the scenario variable `template-result` using template `/data/simple-template.ftl` with parameters:
/data/table-with-scenario-level-variables.table
Then `${template-result}` is equal to `passed: variable is resolved`

Scenario: Verify that variables of different nesting step-levels are cleaned up only for the current level
Meta:
    @issueId 763
When I execute steps while counter is <= `1` with increment `1` starting from `1`:
|step                                                                             |
|When I initialize the scenario variable `test1` with value `${iterationVariable}`|
|When the condition 'true' is true I do                                           |
|{headerSeparator=!,valueSeparator=!}                                             |
|!step                                                             !              |
|!When I initialize the scenario variable `value` with value `test`!              |
|When I initialize the scenario variable `test2` with value `${iterationVariable}`|
|Then `${test1}` is equal to `${test2}`                                           |

Scenario: Verify that variables of different nesting step-levels don't affect each other
Meta:
    @issueId 763
When I execute steps while counter is <= `1` with increment `1` starting from `1`:
|step                                                                               |
|When I initialize the scenario variable `test1` with value `${iterationVariable}`  |
|When the condition 'true' is true I do                                             |
|{headerSeparator=!,valueSeparator=!}                                               |
|!step                                                                             !|
|!Then `${test1}` is equal to `1`                                                  !|
|!When I initialize the scenario variable `iterationVariable` with value `42`      !|
|!When I initialize the scenario variable `test2` with value `${iterationVariable}`!|
|Then `${test2}` is equal to `1`                                                    |
Then `${iterationVariable}` is equal to `42`
