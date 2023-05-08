Meta:
    @epic vividus-plugin-web-app

Scenario: Verify step: "When I type text `$text` in alert and accept it"
Given I am on page with URL `${vividus-test-site-url}/alertprompt.html`
When I click on element located by `buttonName(Survey)`
Then an alert is present
When I type text `VIVIDUS` in alert and accept it
Then text `VIVIDUS is the best!` exists
