Meta:
    @epic vividus-engine
    @feature variables

Lifecycle:
Examples:
${examples-table-path}

Scenario: Generic
Then `<commonVar>` is equal to `common`
Then `<localeBasedVar>` is not equal to `common`

Scenario: Thailand
Meta:
  @locale th
Then `<localeBasedVar>` is equal to `th`

Scenario: North America
Meta:
  @locale us ca
Then `<localeBasedVar>` is equal to `na`
