Scenario: Health check + Geolocation permission
Given I am on page with URL `https://the-internet.herokuapp.com/geolocation`
When I click on element located by `buttonName(Where am I?)`
When I wait until element located by `id(demo)` has text matching `Latitude: .+ Longitude: .+`
