Meta:
    @epic vividus-plugin-web-app

Scenario: Valiadte step: Then there are browser console $logEntries by regex `$regex`
Given I am on a page with the URL '${vividus-test-site-url}'
When I execute javascript `console.error('error')` with arguments:
Then there are browser console ERRORS by regex `.*error.*`
When I execute javascript `console.warn('warninig')` with arguments:
Then there are browser console WARNINGS by regex `.*warninig.*`


Scenario: Verify step: Then there are no browser console $logEntries
When I execute javascript `console.warn('warninig')` with arguments:
Then there are no browser console ERRORS


Scenario: Verify step: Then there are no browser console $logEntries by regex '$regex'
When I execute javascript `console.error('error')` with arguments:
Then there are no browser console ERRORS by regex '.*message.*'
