Scenario: Health check and handling Geolocation permission
Given I am on page with URL `${vividus-test-site-url}/geolocation.html`
When I switch to native context
When I click on element located by `<allowGeolocationButtonLocator>`
When I switch to web view with name that <ruleToFindWebView> `<webViewNamePattern>`
When I wait until element located by `id(element_with_coords)` has text matching `Latitude: .+ Longitude: .+`
When I close browser
Examples:
{metaByRow=true}
| Meta:                   | allowGeolocationButtonLocator                                          | ruleToFindWebView | webViewNamePattern |
| @targetPlatform android | xpath(.//android.widget.Button[@text='Allow while visiting the site']) | is equal to       | CHROMIUM           |
| @targetPlatform ios     | id(Allow)                                                              | contains          | WEBVIEW            |

Scenario: Health check and another way to handle Geolocation permission
Given I am on page with URL `${vividus-test-site-url}/geolocation.html`
When I execute steps in native context:
| step                                                                 |
| When I click on element located by `<allowGeolocationButtonLocator>` |
When I wait until element located by `id(element_with_coords)` has text matching `Latitude: .+ Longitude: .+`
Examples:
{metaByRow=true}
| Meta:                   | allowGeolocationButtonLocator                                          |
| @targetPlatform android | xpath(.//android.widget.Button[@text='Allow while visiting the site']) |
| @targetPlatform ios     | id(Allow)                                                              |
