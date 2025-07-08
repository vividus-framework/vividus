Meta:
    @epic vividus-engine

Scenario: Should filter rows by meta
Then `<city>` is equal to `Vancouver`
Examples:
|Meta:     |city     |
|@locale ca|Vancouver|
|@locale lt|Vilnius  |
|@locale in|Mumbai   |
