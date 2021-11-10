Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Before:
Scope: STORY
Given I am on a page with the URL '${vividus-test-site-url}/dropdowns.html'


Scenario: Validation of step 'Then dropdown located `$locator` contains options: $options'
Then dropdown located `By.id(colors)` contains options:
|state|item |
|false|Red  |
|false|Green|
|true |Blue |


Scenario: Validation of step: 'Then dropdown located `$locator` exists and selected option is `$option`'
Then dropdown located `By.id(colors)` exists and selected option is `Blue`


Scenario: Validation of step: 'When I select `$option` in dropdown located `$locator`'
When I select `Red` in dropdown located `By.id(colors)`
Then dropdown located `id(colors)` exists and selected option is `Red`


Scenario: Validation of step: 'When I add `$option` to selection in dropdown located `$locator`'
Then dropdown located `id(numbers)` contains options:
|state|item |
|false|One  |
|false|Two  |
|true |Three|
When I add `Two` to selection in dropdown located `id(numbers)`
Then dropdown located `id(numbers)` contains options:
|state|item |
|false|One  |
|true |Two  |
|true |Three|
