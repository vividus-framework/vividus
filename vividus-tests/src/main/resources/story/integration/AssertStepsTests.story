Narrative: Story skipped for now. Because to verify the feature we have to use known-issues, but with known issue exit code = 1
For now we cannot override this beahvior. Details in the PR https://github.com/gradle/gradle/pull/10763

Scenario: Set-Up
Given I am on a page with the URL 'https://google.com'

Scenario: Verify step Verify step Then I verify assertions matching '$assertionsPattern'
Then number of elements found by `By.xpath(//*[@*='q'])` is = `1`
Then the text 'Doctor Who?!' exists
Then I verify assertions matching '.*Doctor Who.*'
Then the text 'Extermina-a-a-a-a-te' exists
