Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification When I hover a mouse over an image with the src '$src'
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
When I hover a mouse over an image with the src '/w3css/img_avatar3.png'
Then an element by the xpath './/div[@class='textfade']' exists

Scenario: Step verification When I hover a mouse over an image with the tooltip '$tooltipImage'
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
When I hover a mouse over an image with the tooltip 'Avatar'
Then an element by the xpath './/div[@class='textfade']' exists

Scenario: Step verification When I click on an image with the src '$src'
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/index.html'
When I click on an image with the src 'img/vividus.png'
Then a link by By.xpath(//a[@href='#ElementId']) exists

Scenario: Step verification When I click on an image with the name '$imageName'
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/index.html'
When I click on an image with the name 'vividus-logo'
Then a link by By.xpath(//a[@href='#ElementId']) exists

Scenario: Step verification Then an image with the src '$src' exists
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
Then an image with the src '/w3css/img_avatar3.png' exists

Scenario: Step verification Then a [$state] image with the src '$src' exists
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
Then a [VISIBLE] image with the src '/w3css/img_avatar3.png' exists

Scenario: Step verification Then an image with the src '$src' does not exist
Given I am on a page with the URL 'https://www.w3schools.com/tags/tryit.asp?filename=tryhtml_link_image'
When I switch to a frame with the attribute 'id'='iframeResult'
When I click on an image with the src 'logo_w3s.gif'
Then an image with the src 'logo_w3s.gif' does not exist

Scenario: Step verification Then an image with the src containing '$srcpart' exists
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
Then an image with the src containing '3css/img_avatar3.png' exists

Scenario: Step verification Then an image with the tooltip '$tooltip' and src containing '$srcpart' exists
Given I am on a page with the URL 'https://www.wpbeginner.com/beginners-guide/image-alt-text-vs-image-title-in-wordpress-whats-the-difference/'
Then an image with the tooltip 'Alternate text displayed in a broken image container' and src containing 'ploads/2014/10/broken-img-alt-text.jpg' exists

Scenario: Step verification Then an image with the src '$imageSrc' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.wpbeginner.com/beginners-guide/image-alt-text-vs-image-title-in-wordpress-whats-the-difference/'
Then an image with the src 'https://cdn3.wpbeginner.com/wp-content/uploads/2014/10/broken-img-alt-text.jpg' and tooltip 'Alternate text displayed in a broken image container' exists

Scenario: Step verification Then a [$state] image with the src '$imageSrc' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
Then a [VISIBLE] image with the src '/w3css/img_avatar3.png' and tooltip 'Avatar' exists

Scenario: Step verification Then a [$state] image with the src containing '$srcpart' exists
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
Then a [VISIBLE] image with the src containing '3css/img_avatar3.png' exists

Scenario: Step verification Then a [$state] image with the tooltip '$tooltipImage' exists
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
Then a [VISIBLE] image with the tooltip 'Avatar' exists

Scenario: Step verification Then an image with the tooltip '$tooltipImage' exists
Given I am on a page with the URL 'https://www.w3schools.com/howto/howto_css_image_overlay.asp'
When I change context to an element by By.xpath(.//div[@class='containerfade'])
Then an image with the tooltip 'Avatar' exists
