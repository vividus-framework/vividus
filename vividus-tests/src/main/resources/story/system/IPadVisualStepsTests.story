Scenario: Validate element is ignored when native header to cut is set
Meta: @layout tablet
Given I am on a page with the URL 'https://example.com'
When I COMPARE_AGAINST baseline with name `ipad-ignore-element` ignoring:
|ELEMENT       |
|By.tagName(h1)|
using screenshot configuration:
|nativeHeaderToCut|
|71               |
