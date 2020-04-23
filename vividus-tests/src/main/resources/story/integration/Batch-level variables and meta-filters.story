Description: Integration tests for batch-level variables and meta-filters

Meta:
    @epic vividus-bdd-engine
    @feature variables

Lifecycle:
Examples:
/data/tables/locales/${locale}/locale-based.table

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
