Description: Integration tests for HtmlSteps class

Meta:
    @epic vividus-plugin-html

Lifecycle:
Before:
Scope: STORY
Given I initialize story variable `html` with value
`
<html>
  <head>
    <title>Page Title</title>
    <script>//<![CDATA[Here comes the data//]]></script>
  </head>
  <body>
    <h1>This is a Heading</h1>
    <p title="paragraph">This is a paragraph.</p>
  </body>
</html>
`

Scenario: Verify step 'Then element found by $locatorType `$locator` in HTML `$html` contains text `$expectedText`'
Then element found by <locatorType> `<locator>` in HTML `${html}` contains text `This is a paragraph.`
Examples:
|locatorType |locator  |
|CSS selector|body > p |
|XPath       |//p      |


Scenario: Verify step 'When I save `$attributeName` attribute value of element found by $locatorType `$locator` in HTML `$html` to $scopes variable `$variableName`'
When I save `title` attribute value of element found by <locatorType> `<locator>` in HTML `${html}` to scenario variable `title`
Then `${title}` is equal to `paragraph`
Examples:
|locatorType |locator  |
|CSS selector|body > p |
|XPath       |//p      |


Scenario: Verify step 'When I save $dataType of element found by $locatorType `$locator` in HTML `$html` to $scopes variable `$variableName`'
When I save data of element found by <locatorType> `<script>` in HTML `${html}` to scenario variable `data`
When I save text of element found by <locatorType> `<header>` in HTML `${html}` to scenario variable `text`
Then `${data}` is equal to `//<![CDATA[Here comes the data//]]>`
Then `${text}` is equal to `This is a Heading`
Examples:
|locatorType |script  |header|
|CSS selector|script  |h1    |
|XPath       |//script|//h1  |


Scenario: Verify step: 'Then number of elements found by $locatorType `$cssSelector` in HTML `$html` is $comparisonRule `$quantity`'
Then number of elements found by <locatorType> `<locator>` in HTML `${html}` is = `1`
Examples:
|locatorType |locator                            |
|CSS selector|*:containsOwn(Page Title)          |
|XPath       |//*[contains(text(), 'Page Title')]|
