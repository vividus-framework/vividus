Description: Integration tests for XmlSteps class

Meta:
    @epic vividus-plugin-xml

Lifecycle:
Before:
Scope: STORY
When I initialize the story variable `xml` with value
`
<note>
  <to>Tove</to>
  <from>Jani</from>
  <heading>Reminder</heading>
  <body>Don't forget me this weekend!</body>
</note>
`

Scenario: Verify step 'Then XML `$xml` is equal to `$expectedXml`'
Then XML `${xml}` is equal to
`
<note>
  <to>Tove</to>
  <from>Jani</from>
  <heading>Reminder</heading>
  <body>Don't forget me this weekend!</body>
</note>
`

Scenario: Verify step 'When I save data found by xpath `$xpath` in XML `$xml` to $scopes variable `$variableName`'
When I save data found by xpath `//body` in XML `${xml}` to scenario variable `body`
Then `${body}` is equal to `<body>Don't forget me this weekend!</body>`

Scenario: Verify step 'When I transform XML `$xml` with `$xslt` and save result to $scopes variable `$variableName`'
When I transform XML `${xml}` with `#{loadResource(note-to-string.xsl)}` and save result to scenario variable `result`
Then `${result}` is equal to `Tove > Jani;`

Scenario: Verify step 'Then XML `$xml` contains element by xpath `$xpath`'
Then XML `${xml}` contains element by xpath `/note[from = "Jani"]`

Scenario: Verify step 'Then XML `$xml` is valid against XSD `$xsd`'
Then XML `${xml}` is valid against XSD `#{loadResource(note.xsd)}`
