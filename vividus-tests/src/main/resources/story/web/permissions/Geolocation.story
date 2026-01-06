Scenario: Health check + Trigger Geolocation permission
Given I am on page with URL `${vividus-test-site-url}/geolocation.html`

Scenario: Allow to use device's location
Meta:
    @targetPlatform android
When I switch to native context
When I click on element located by `xpath(.//android.widget.Button[@text='Allow while visiting the site'])`
When I switch to web view with name that is equal to `CHROMIUM`

Scenario: Allow to use device's location
Meta:
    @targetPlatform ios
When I switch to native context
When I click on element located by `id(Allow)`
When I switch to web view with name that contains `WEBVIEW`

Scenario: Validate Geolocation appears
When I wait until element located by `id(element_with_coords)` has text matching `Latitude: .+ Longitude: .+`
