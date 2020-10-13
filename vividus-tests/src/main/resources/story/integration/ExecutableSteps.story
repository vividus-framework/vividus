Description: Integration tests for ExecutableSteps class.

Meta:
    @epic vividus-core
    @feature executable-steps

Scenario: Step verification When I iterate while counter is $comparisonRule `$limit` with increment `$increment` starting from `$seed`:$stepsToExecute

When I initialize the STORY variable `iterator` with value `0`
When I execute steps while counter is less than or equal to `10` with increment `3` starting from `1`:
{headerSeparator=!, valueSeparator=!}
!step                                                                                                  !
!When I initialize the SCENARIO variable `key-${iterationVariable}` with value `${iterationVariable}`  !
!When I execute steps while counter is less than or equal to '2' with increment '1' starting from '1': !
!|step                                                                                                |!
!|When I initialize the STORY variable `iterator` with value `#{eval(${iterator} + 1)}`               |!
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

When I initialize the STORY variable `iterator` with value `0`
When the condition `true` is true I do
{headerSeparator=!, valueSeparator=!}
!step                                                                                   !
!Then `${iterator}` is = `0`                                                            !
!When I initialize the STORY variable `iterator` with value `#{eval(${iterator} + 1)}`  !
!When the condition 'true' is true I do                                                 !
!|step                                                                                 |!
!|Then `${iterator}` is = `1`                                                          |!
!|When I initialize the STORY variable `iterator` with value `#{eval(${iterator} + 1)}`|!
Then `${iterator}` is = `2`


Scenario: Verify step: When variable `$name` is not set I do:$stepsToExecute

When I initialize the STORY variable `iterator` with value `0`
When variable `varName` is not set I do:
{headerSeparator=!, valueSeparator=!}
!step                                                                                   !
!Then `${iterator}` is = `0`                                                            !
!When I initialize the STORY variable `iterator` with value `#{eval(${iterator} + 1)}`  !
!When variable 'varName' is not set I do:                                               !
!|step                                                                                 |!
!|Then `${iterator}` is = `1`                                                          |!
!|When I initialize the STORY variable `iterator` with value `#{eval(${iterator} + 1)}`|!
Then `${iterator}` is = `2`


Scenario: Verify step: When I `$number` times do:$stepsToExecute

When I initialize the STORY variable `iterator` with value `0`
When I `1` times do:
{headerSeparator=!, valueSeparator=!}
!step                                                                                   !
!Then `${iterator}` is = `0`                                                            !
!When I initialize the STORY variable `iterator` with value `#{eval(${iterator} + 1)}`  !
!When I '1' times do:                                                                   !
!|step                                                                                 |!
!|Then `${iterator}` is = `1`                                                          |!
!|When I initialize the STORY variable `iterator` with value `#{eval(${iterator} + 1)}`|!
Then `${iterator}` is = `2`

Scenario: Verify step: When I execute steps at most $max times while variable `$variableName` is $comparisonRule `$expectedValue`:$stepsToExeute
When I initialize the scenario variable `var` with value `0`
When I execute steps at most 5 times while variable `var` is < `3`:
|step                                                                          |
|When I initialize the scenario variable `var` with value `#{eval(${var} + 1)}`|
Then `${var}` is = `3`
