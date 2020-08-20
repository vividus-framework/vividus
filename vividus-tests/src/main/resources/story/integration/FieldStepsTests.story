Description: Integration tests for FieldSteps class. Page for verification origin:
https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input

Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Examples:
|inputLocator    |inputId  |
|By.id(textInput)|textInput|

Scenario: Set-Up
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input$samples/caret-color'

Scenario: Step verification Then field located `$locator` does not exist
Then field located `By.xpath(//noSuchField):a->filter.textPart(text)` does not exist

Scenario: Step verification Then field located `$locator` exists
Then field located `<inputLocator>` exists

Scenario: Step verification When I add `$text` to field located `$locator`
Then field value is ``
When I enter `text` in field located `<inputLocator>`
When I add `text` to field located `<inputLocator>`
Then field value is `texttext`

Scenario: Step verification When I clear field located `$locator`
When I clear field located `<inputLocator>`
When I perform javascript 'return document.querySelector('#textInput').value' and save result to the 'Scenario' variable 'fieldValue'
Then field value is ``

Scenario: Step verification When I enter `$text` in field located `$locator`
When I enter `text` in field located `<inputLocator>`
Then field value is `text`

!-- Composites down there

Scenario: Step verification When I clear field located `$locator` using keyboard
When I clear field located `<inputLocator>` using keyboard
Then field value is ``

Scenario: Step verification Then a field with the name '$fieldName' exists
Then a field with the name '<inputId>' exists

Scenario: Step verification When I add '$text' to a field with the name '$fieldName'
When I add 'text' to a field with the name '<inputId>'
Then field value is `text`

Scenario: Step verification When I clear a field with the name '$fieldName'
When I clear a field with the name '<inputId>'
Then field value is ``

Scenario: Step verification When I add '$text' to a field by the xpath '$xpath'
When I add 'text' to a field by the xpath './/input[@id='<inputId>']'
Then field value is `text`

Scenario: Step verification When I clear a field with the name '$fieldName' using keyboard
When I clear a field with the name '<inputId>' using keyboard
Then field value is ``

Scenario: Step verification When I enter '$text' in a field with the name '$fieldName'
When I enter 'text' in a field with the name '<inputId>'
Then field value is `text`

Scenario: Step verification When I clear a field by the xpath '$xpath'
When I clear a field by the xpath './/input[@id='<inputId>']'
Then field value is ``

Scenario: Step verification When I enter '$text' in a field by the xpath '$xpath'
When I enter 'text' in a field by the xpath './/input[@id='<inputId>']'
Then field value is `text`

Scenario: Step verification When I clear a field by the xpath '$xpath' using keyboard
When I clear a field by the xpath './/input[@id='<inputId>']' using keyboard
Then field value is ``

Scenario: Step verification Then a field with the name '$fieldName' does not exist
Then a field with the name 'noSuchFieldOnThePage' does not exist

Scenario: Step verification Then a [$state] field with the name '$fieldName' and text '$text' exists
Then a [VISIBLE] field with the name '<inputId>' and text '' exists

Scenario: Step verification Then a field with the name '$fieldName' and text containing '$textpart' exists
When I add 'text' to a field with the name '<inputId>'
Then a field with the name '<inputId>' and text containing 'te' exists

Scenario: Step verification Then a field with the name '$fieldName' and text '$text' does not exist
Then a field with the name '<inputId>' and text 'noTextInTheFieldActually' does not exist

Scenario: Step verification Then a field with the name '$fieldName' and text '$text' exists
Then a field with the name '<inputId>' and text 'text' exists

Scenario: Step verification Then a field with the name '$fieldName' and placeholder text '$text' exists
Then a field with the name '<inputId>' and placeholder text '' exists

Scenario: Step verification Then a [$state] field with the name '$fieldName' and placeholder text '$text' exists
Then a [VISIBLE] field with the name '<inputId>' and placeholder text '' exists

Scenario: Step verification Then a field with the name '$fieldName' placeholder text '$text' does not exist
Then a field with the name '<inputId>' placeholder text 'noPlaceholderAtAll' does not exist

Scenario: Step verification Then a [$state] field with the name '$fieldName' exists
Then a [VISIBLE] field with the name '<inputId>' exists
