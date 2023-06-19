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
Given deprecated step without parameters
Examples:
|header1  |header2  |
|value1-1 |value2-1 |
|value2-1 |value2-2 |


Scenario: Test replacing of deprecated step with parameters

Given deprecated step with parameters: placeholder2-`placeholder3`-`placeholder1`
Scenario: Test replacing of deprecated nested steps
Given I initialize scenario variable `variable` with value `#{generateDate(PT0S)}`
Given deprecated code step with parameters <mods>-`${variable}`-`#{generate(DungeonsAndDragons.monsters)}` and SubSteps:
|step|

!-- comment
Given deprecated code step with parameters placeholder3-0Lvl-`placeholder2-0Lvl`-`placeholder1-0Lvl` and SubSteps:
{headerSeparator=!, valueSeparator=!}
!step!
!Given deprecated step without parameters                                                                                 !
!Given deprecated code step with parameters placeholder3-1stLvl-'placeholder2-1stLvl'-'placeholder1-1stLvl' and SubSteps: !
!|step                                                                                                                   |!
!|Given deprecated step without parameters but not for replace                                                           |!
!|Given deprecated step with parameters: placeholder2-2ndLvl-`placeholder3-2ndLvl`-`placeholder1-2ndLvl`                 |!
!|Given deprecated step without parameters                                                                               |!
