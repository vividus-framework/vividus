Description: Integration tests for DropdownSteps class.

Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Before:
Scope: STORY
Given I am on a page with the URL '${vividus-test-site-url}/dropdowns.html'


Scenario: Validation of step: 'Then a drop down with the name '$dropDownName' exists'
Then number of elements found by `tagName(select)` is = `2`
Then a drop down with the name 'colors' exists


Scenario: Validation of step 'Then a drop down with the name '$dropDownName' contains the items: $dropDownItems'
Then a drop down with the name 'colors' contains the items:
|state|item |
|false|Red  |
|false|Green|
|true |Blue |


Scenario: Validation of step 'Then dropdown located `$locator` contains options: $options'
Then dropdown located `id(colors)` contains options:
|state|item |
|false|Red  |
|false|Green|
|true |Blue |


Scenario: Validation of step: 'Then a drop down with the name '$dropDownName' does not exist'
Then a drop down with the name 'colorz' does not exist


Scenario: Validation of step: 'Then a [$state] drop down with the name '$dropDownName' exists'
Then a [VISIBLE] drop down with the name 'colors' exists


Scenario: Validation of step: 'Then a drop down with the name '$dropDownName' and text '$dropDownText' exists'
Then a drop down with the name 'colors' and text 'Blue' exists


Scenario: Validation of step: 'Then dropdown located `$locator` exists and selected option is `$option`'
Then dropdown located `id(colors)` exists and selected option is `Blue`


Scenario: Validation of step: 'When I select '$text' from a drop down with the name '$dropDownListName''
When I select 'Red' from a drop down with the name 'colors'
Then a drop down with the name 'colors' and text 'Red' exists
When I refresh the page


Scenario: Validation of step: 'When I add '$text' to selection in a drop down with the name '$dropDownListName''
When I add 'Two' to selection in a drop down with the name 'numbers'
Then a drop down with the name 'numbers' contains the items:
|state|item |
|false|One  |
|true |Two  |
|true |Three|


Scenario: Validation of step: 'When I add `$option` to selection in dropdown located `$locator`'
When I add `Two` to selection in dropdown located `id(numbers)`
Then dropdown located `id(numbers)` contains options:
|state|item |
|false|One  |
|true |Two  |
|true |Three|


Scenario: Validation of step: 'When I select `$text` from drop down located `$locator`'
When I select `Red` from drop down located `id(colors)`
Then a drop down with the name 'colors' and text 'Red' exists


Scenario: Validation of step: 'When I select `$option` in dropdown located `$locator`'
When I select `Red` in dropdown located `id(colors)`
Then a drop down with the name 'colors' and text 'Red' exists
