Meta:
    @epic vividus-plugin-azure-functions
    @skip

Scenario: Trigger a function
When I trigger function `HttpTrigger1` from function app `vivdus-http-function` in resource group `vividus` with payload:
and save response into scenario variable `result`
Then `202` is equal to `${result.status-code}`
