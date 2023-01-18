Meta:
    @epic vividus-plugin-web-app
    @requirementId 2112

Scenario: Verify steps: "When I scan a barcode from screen and save result to $scopes variable `$variableName`"
Given I am on page with URL `${vividus-test-site-url}/qrCode.html`
When I scan barcode from screen and save result to scenario variable `qrCodeLink`
Then `${qrCodeLink}` is = `https://github.com/vividus-framework/vividus`

Scenario: Verify steps: "When I scan barcode from context and save result to $scopes variable `$variableName`"
Meta:
    @requirementId 2687
Given I am on page with URL `${vividus-test-site-url}/qrModal.html`
When I click on element located by `id(modalButton)`
When I change context to element located by `xpath(//div[@class='modal-content'])` in scope of current context
When I scan barcode from context and save result to scenario variable `qrCodeLink`
Then `${qrCodeLink}` is = `https://github.com/vividus-framework/vividus`
