Description: Integration tests for ButtonSteps class.

Meta:
    @group vividus-plugin-web-app

Lifecycle:
Examples:
{transformer=FROM_LANDSCAPE}
|buttonURL           |https://mdn.mozillademos.org/en-US/docs/Web/API/MouseEvent/button$samples/Example                                 |
|radioButtonsURL     |https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?|

Scenario: Step verification Then a radio button with the name '$radioOption' exists
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?'
Then a radio button with the name 'Email' exists

Scenario: Step verification Then a [$state] radio button with the name '$radioOption' exists
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?'
When I select a radio button with the name 'Email'
Then a [SELECTED] radio button with the name 'Email' exists

!-- Composites down there

Scenario: Step verification a button with the name '$buttonName' exists
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/button$samples/Example?'
Then a button with the name 'button' exists

Scenario: Step verification a [$state] button with the name '$buttonName' exists
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/button$samples/Example?'
Then a [ENABLED] button with the name 'button' exists

Scenario: Step verification a button with the name '$buttonName' does not exist
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/button$samples/Example?'
Then a button with the name 'fake-button' does not exist
