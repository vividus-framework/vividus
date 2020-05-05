Description: Integration tests for ActionSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Action verification MOVE_TO
Given I am on a page with the URL 'https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_onmousemove_over_enter'
When I switch to a frame with the attribute 'id'='iframeResult'
When I execute sequence of actions:
|type   |argument                                                   |
|MOVE_TO|By.xpath(//div[contains(., 'onmouseover: Mouse over me!')])|
Then the text 'onmouseover: 1' exists
