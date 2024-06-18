Description: Integration tests for ChromeExperimentalSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Download and verify excel file
Given I am on page with URL `${vividus-test-site-url}/download.html`
When I click on element located by `linkText(Download sample Excel doc)`
When I download file with name matching `excel_sample` from browser downloads and save its content to SCENARIO variable `excelFile`
Then `${excelFile}` contains excel sheet with name `Sheet1` and records:
|cellsRange|valueRegex |
|A1        |A          |
|B2        |12.0       |
|C3        |23.0       |
