Description: Integration tests for optional environments

Meta:
    @epic vividus-engine
    @feature configuration

Scenario: Verify optional existing environment is loaded
Then `${optional-environment}` is equal to `loaded`
