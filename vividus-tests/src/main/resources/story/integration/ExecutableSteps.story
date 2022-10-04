Description: Integration tests for ExecutableSteps class.

Meta:
    @epic vividus-core
    @feature executable-steps

Scenario: Step verification When I iterate while counter is $comparisonRule `$limit` with increment `$increment` starting from `$seed`:$stepsToExecute

Given I initialize story variable `iterator` with value `0`
When I execute steps while counter is less than or equal to `10` with increment `3` starting from `1`:
{headerSeparator=!, valueSeparator=!}
!step                                                                                                  !
!Given I initialize scenario variable `key-${iterationVariable}` with value `${iterationVariable}`  !
!When I execute steps while counter is less than or equal to '2' with increment '1' starting from '1': !
!|step                                                                                                |!
!|Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`               |!
Then `${key-1}` is equal to `1`
Then `${key-2}` is equal to `${key-2}`
Then `${key-3}` is equal to `${key-3}`
Then `${key-4}` is equal to `4`
Then `${key-5}` is equal to `${key-5}`
Then `${key-6}` is equal to `${key-6}`
Then `${key-7}` is equal to `7`
Then `${key-8}` is equal to `${key-8}`
Then `${key-9}` is equal to `${key-9}`
Then `${key-10}` is equal to `10`
Then `${key-11}` is equal to `${key-11}`
Then `${key-12}` is equal to `${key-12}`
Then `${key-13}` is equal to `${key-13}`
Then `${iterator}` is = `8`


Scenario: Verify step: When the condition `$condition` is true I do$steps

Given I initialize story variable `iterator` with value `0`
When the condition `true` is true I do
{headerSeparator=!, valueSeparator=!}
!step                                                                                   !
!Then `${iterator}` is = `0`                                                            !
!Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`     !
!When the condition 'true' is true I do                                                 !
!|step                                                                                 |!
!|Then `${iterator}` is = `1`                                                          |!
!|Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`   |!
Then `${iterator}` is = `2`

Scenario: Verify step: "When the condition `$condition` is true I do$steps" (TRUE conditions)
Meta:
    @issueId 1746
Given I initialize scenario variable `conditional-variable` with value `value`
When the condition `<condition>` is true I do
|step|
|Given I initialize scenario variable `conditional-variable` with value `conditional-value`|
Then `${conditional-variable}` is = `conditional-value`
Examples:
|condition|
|1        |
|t        |
|T        |
|true     |
|TRUE     |
|on       |
|ON       |
|yes      |
|YES      |
|Y        |
|y        |

Scenario: Verify step: "When the condition `$condition` is true I do$steps" (FALSE conditions)
Meta:
    @issueId 1746
Given I initialize scenario variable `conditional-variable` with value `value`
When the condition `<condition>` is true I do
|step|
|Given I initialize scenario variable `conditional-variable` with value `conditional-value`|
Then `${conditional-variable}` is = `value`
Examples:
|condition|
|0        |
|f        |
|F        |
|false    |
|FALSE    |
|off      |
|OFF      |
|no       |
|NO       |
|N        |
|n        |


Scenario: Verify step: When variable `$name` is not set I do:$stepsToExecute

Given I initialize story variable `iterator` with value `0`
When variable `varName` is not set I do:
{headerSeparator=!, valueSeparator=!}
!step                                                                                !
!Then `${iterator}` is = `0`                                                         !
!Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`  !
!When variable 'varName' is not set I do:                                            !
!|step                                                                              |!
!|Then `${iterator}` is = `1`                                                       |!
!|Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`|!
Then `${iterator}` is = `2`


Scenario: Verify step: When I execute steps:$stepsToExecute
Meta:
    @requirementId 1800

Given I initialize story variable `iterator` with value `0`
When I execute steps:
{headerSeparator=!, valueSeparator=!}
!step                                                                                !
!Then `${iterator}` is = `0`                                                         !
!Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`  !
!When I execute steps:                                                               !
!|step                                                                              |!
!|Then `${iterator}` is = `1`                                                       |!
!|Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`|!
Then `${iterator}` is = `2`


Scenario: Verify step: When I `$number` times do:$stepsToExecute

Given I initialize story variable `iterator` with value `0`
When I `1` times do:
{headerSeparator=!, valueSeparator=!}
!step                                                                                !
!Then `${iterator}` is = `0`                                                         !
!Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`  !
!When I '1' times do:                                                                !
!|step                                                                              |!
!|Then `${iterator}` is = `1`                                                       |!
!|Given I initialize story variable `iterator` with value `#{eval(${iterator} + 1)}`|!
Then `${iterator}` is = `2`

Scenario: Verify step: When I execute steps at most $max times while variable `$variableName` is $comparisonRule `$expectedValue`:$stepsToExeute
Given I initialize scenario variable `var` with value `5`
When I execute steps at most 5 times while variable `var` is < `10`:
|step                                                                       |
|Given I initialize scenario variable `var` with value `#{eval(${var} + 1)}`|
Then `${var}` is = `10`

Scenario: Verify step: When I execute steps with delay `$delay` at most $max times while variable `$variableName` is $comparisonRule `$expectedValue`:$stepsToExeute
Meta:
    @requirementId 1235
Given I initialize scenario variable `var` with value `5`
When I execute steps with delay `PT0.5S` at most 2 times while variable `var` is < `7`:
|step                                                                       |
|Given I initialize scenario variable `var` with value `#{eval(${var} + 1)}`|
Then `${var}` is = `7`
