Scenario: Validate element is ignored when native header to cut is set
Meta: @layout tablet
Given I am on page with URL `https://web.archive.org/web/20251030235939id_/http://example.com/`
When I COMPARE_AGAINST baseline with name `ipad-ignore-element` ignoring:
|ELEMENT       |
|By.tagName(h1)|
using screenshot configuration:
|nativeHeaderToCut|
|71               |
