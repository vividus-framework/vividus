Meta:
    @skip

Lifecycle:
Before:
Scope: STORY
Given deprecated step without replace
After:
Scope: SCENARIO
Given deprecated code without replace
Examples:
|mods                  |
|Sword of the Perithia |
|Sea of Destiny        |

Scenario: Test replacing of deprecated step without parameters
Given deprecated step without parameters but not for replace
Given actual step without parameters
Examples:
|header1  |header2  |
|value1-1 |value2-1 |
|value2-1 |value2-2 |


Scenario: Test replacing of deprecated step with parameters

Given actual step with parameters: placeholder1-`placeholder2`-`placeholder3`
Scenario: Test replacing of deprecated nested steps
Given I initialize scenario variable `variable` with value `#{generateDate(PT0S)}`
When actual code step with parameters `#{generate(DungeonsAndDragons.monsters)}`-`${variable}`-`<mods>` and SubSteps:
|step|

!-- comment
When actual code step with parameters `placeholder1-0Lvl`-`placeholder2-0Lvl`-`placeholder3-0Lvl` and SubSteps:
{headerSeparator=!, valueSeparator=!}
!step!
!Given actual step without parameters                                                                                 !
!When actual code step with parameters `placeholder1-1stLvl`-`placeholder2-1stLvl`-`placeholder3-1stLvl` and SubSteps: !
!|step                                                                                                                   |!
!|Given deprecated step without parameters but not for replace                                                           |!
!|Given actual step with parameters: placeholder1-2ndLvl-`placeholder2-2ndLvl`-`placeholder3-2ndLvl`                 |!
!|Given actual step without parameters                                                                               |!
