Description: Integration tests for encryption functionality

Meta:
    @epic vividus-core
    @feature encryption

Scenario: Verify decrypted value is loaded from properties
Then `${encrypted-variable}` is equal to `my-secret`
