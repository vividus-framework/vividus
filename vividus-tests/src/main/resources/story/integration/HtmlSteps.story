Description: Integration tests for HtmlSteps class

Meta:
    @group vividus-plugin-html

Lifecycle:
Before:
Scope: STORY
When I initialize the story variable `html` with value
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

Scenario: Verify step 'Then HTML `$html` contains element by CSS selector `$cssSelector`'
Then HTML `${html}` contains element by CSS selector `body > h1`

Scenario: Verify step 'Then HTML `$html` contains data `$expectedData` by CSS selector `$cssSelector`'
Then HTML `${html}` contains data `This is a paragraph.` by CSS selector `body > p`

Scenario: Verify step 'When I save `$attributeName` attribute value of element from HTML `$html` by CSS selector
`$cssSelector` to $scopes variable `$variableName`'
When I save `title` attribute value of element from HTML `${html}` by CSS selector `body > p` to scenario variable `title`
Then `${title}` is equal to `paragraph`

Scenario: Verify step 'When I save $dataType of element from HTML `$html` by CSS selector `selector` to
 scope variable `variableName`'
When I save data of element from HTML `${html}` by CSS selector `script` to scenario variable `data`
When I save text of element from HTML `${html}` by CSS selector `h1` to scenario variable `text`
Then `${data}` is equal to `//<![CDATA[Here comes the data//]]>`
Then `${text}` is equal to `This is a Heading`
