Meta:
    @epic vividus-plugin-web-app
    @requirementId 2112

Scenario: Verify steps: "When I scan a barcode from screen and save result to $scopes variable `$variableName`"
Given I am on a page with the URL '${vividus-test-site-url}/qrCode.html'
When I scan barcode from screen and save result to scenario variable `qrCodeLink`
Then `${qrCodeLink}` is = `https://github.com/vividus-framework/vividus`
