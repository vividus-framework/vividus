Meta:
    @epic vividus-plugin-web-app

Scenario: Verify deprecated step: "When I get the URL path and set it to the $scopes variable '$variable'"
Given I am on page with URL `${vividus-test-site-url}/inputs.html?id=1`
When I get the URL path and set it to the SCENARIO variable 'urlpath'
Given I initialize SCENARIO variable `exprurlpath` with value `#{extractPathFromUrl(${current-page-url})}`
Then `${exprurlpath}` is = `${urlpath}`

Scenario: Verify deprecated step: "When I get the value from the URL and set it to the '$scopes' variable '$variable'"
Given I am on page with URL `${vividus-test-site-url}/inputs.html?id=1#test`
When I get the value from the URL and set it to the 'SCENARIO' variable 'urlvalue'
Given I initialize SCENARIO variable `exprurlvalue` with value `#{replaceFirstByRegExp(.*\/(?=[^\/?#]+(?:\?.+)?(?:#.*)?$),,${current-page-url})}`
Then `${exprurlvalue}` is = `${urlvalue}`

Scenario: Verify step: "I save table to $scopes variable `$variableName`"
Given I am on page with URL `${vividus-test-site-url}/table.html`
When I change context to element located `xpath(//table)`
When I save table to SCENARIO variable `table`
Then `${table}` is equal to table:
|A |B |C |
|A1|B1|C1|
|A2|B2|C2|
|A3|B3|C3|

Scenario: Verify deprecated step: "When I save table to $scopes variable '$variableName'"
Given I am on page with URL `${vividus-test-site-url}/table.html`
When I change context to element located `xpath(//table)`
When I save table to SCENARIO variable 'table'
Then `${table[1]}` is equal to table ignoring extra columns:
|A |C |
|A2|C2|

Scenario: Verify step: "When I save number of open tabs to $scopes variable `$variableName`"
Given I am on page with URL `${vividus-test-site-url}/table.html`
When I open new tab
When I save number of open tabs to SCENARIO variable `tabscount`
When I close current tab
Then `${tabscount}` is = `2`

Scenario: Verify deprecated step: "When I get the number of open windows and set it to the $scopes variable '$variable'"
Given I am on page with URL `${vividus-test-site-url}`
When I open URL `${vividus-test-site-url}` in new tab
When I get the number of open windows and set it to the SCENARIO variable 'tabscount'
When I close current tab
Then `${tabscount}` is = `2`

Scenario: Verify deprecated step: "When I get the URL value of a video with sequence number '$number' and set it to the '$scopes' variable '$variable'"
Given I am on page with URL `${vividus-test-site-url}/severalvideos.html`
When I change context to element located `xpath(//div[@class='container'])`
When I get the URL value of a video with sequence number '2' and set it to the 'SCENARIO' variable 'vidSrc1'
When I save `src` attribute value of element located `xpath((div[contains(@class,'video')]/iframe)[2])` to SCENARIO variable `vidSrc2`
Then `${vidSrc1}` is = `${vidSrc2}`
