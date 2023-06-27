Meta:
    @epic vividus-plugin-web-app

Scenario: Valiadte step: Then there are browser console $logEntries by regex `$regex`
Given I am on page with URL `${vividus-test-site-url}`
When I execute javascript `console.error('error')` with arguments:
Then there are browser console ERRORS by regex `.*error.*`
When I execute javascript `console.warn('warninig')` with arguments:
Then there are browser console WARNINGS by regex `.*warninig.*`
When I execute javascript `console.log('info')` with arguments:
Then there are browser console INFOS by regex `.*info.*`


Scenario: Verify step: Then there are no browser console $logEntries
When I execute javascript `console.warn('warninig')` with arguments:
Then there are no browser console INFOS
Then there are no browser console ERRORS


Scenario: Verify steps: Then there are no browser console $logEntries by regex `$regex`, Then there are no browser console $logEntries by regex '$regex'
When I execute javascript `console.error('error')` with arguments:
Then there are no browser console ERRORS by regex `.*message.*`
!-- Deprecated
Then there are no browser console ERRORS by regex '.*message.*'


Scenario: Verify step: When I wait until browser console $logEntries by regex `$regex` appear and save all entries into $scopes variable `$variable`
When I execute javascript `console.log('immediate')` with arguments:
When I execute javascript `setTimeout(() => console.log("delayed"), 5000);` with arguments:
When I wait until browser console infos by regex `.*delayed.*` appear and save all entries into scenario variable `logs`
Then there are no browser console INFOS by regex `.*immediate.*`
Then `${logs}` matches `.*immediate.*`
Then `${logs}` matches `.*delayed.*`
