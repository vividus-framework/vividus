Meta:
    @epic vividus-plugin-azure-functions

Scenario: Trigger a function
When I trigger function `function-by-http-trigger` from function app `vivdus-function` in resource group `vividus` with payload:
and save response into scenario variable `result`
Then `202` is equal to `${result.status-code}`
