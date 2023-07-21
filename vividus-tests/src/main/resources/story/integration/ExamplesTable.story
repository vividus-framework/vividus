Meta:
    @epic vividus-core
    @feature examples-table

Lifecycle:
Examples:
|a|
|y|

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

Scenario: Resolve story-level ExamplesTable placeholders in scenario-level ExamplesTable placeholder name
Then `<x<a>z>` is equal to `hello`
Examples:
|xyz  |
|hello|

Scenario: Resolve ExamplesTable placeholders with dot character
Then `<<x>>` is equal to `hello`
Examples:
|x   |xy.z |
|xy.z|hello|

Scenario: Load ExamplesTable with user's resource loader configuration
Then `<localeBasedVar>` is equal to `na`
Examples:
/data/tables/locales/locale-based.table
