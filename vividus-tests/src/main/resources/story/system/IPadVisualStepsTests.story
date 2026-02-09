Meta: @layout tablet

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `https://example.com`

Scenario: Validate full page without native header
When I COMPARE_AGAINST baseline with name `ipad-full-page` using screenshot configuration:
| nativeHeaderToCut |
| 71                |

Scenario: Validate element is ignored when native header to cut is set
When I COMPARE_AGAINST baseline with name `ipad-ignore-element` ignoring:
|ELEMENT       |
|By.tagName(h1)|
using screenshot configuration:
|nativeHeaderToCut|
|71               |
