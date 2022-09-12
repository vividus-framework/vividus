Meta:
    @epic vividus-core
    @feature examples-table

Scenario: Use ExamplesTable key with whitespace
Meta:
    @issueId 2255
Then `<my key>` is equal to `e0edf9`
Examples:
|my key|
|e0edf9|

Scenario: Use ExamplesTable with line breaks in values
Then `<header>` matches `line 1\r?\nline 2`
Examples:
{processEscapeSequences=true}
|header        |
|line 1\nline 2|
