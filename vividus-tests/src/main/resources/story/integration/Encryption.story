Description: Integration tests for encryption functionality

Meta:
    @group encryption

Scenario: Verify decrypted value is loaded from properties
Then `${encrypted-variable}` is equal to `my-secret`
