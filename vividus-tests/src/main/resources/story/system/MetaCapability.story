Meta:
    @capability.browserName IE
    @capability.browserVersion 11.0

Scenario: Browser should correspond options set in meta
Given I am on a page with the URL 'https://www.whatismybrowser.com'
Then the text 'Internet Explorer 11' exists
