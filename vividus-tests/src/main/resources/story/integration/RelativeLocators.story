Meta:
    @epic vividus-plugin-web-app
    @feature locators

Scenario: Verify easy directions success
Given I am on page with URL `${vividus-test-site-url}/relativeLocator.html`
Then number of elements found by `relative(id(block1)>>toLeftOf(id(block2)))` is = `1`
Then number of elements found by `relative(id(block7)>>toRightOf(id(block2)))` is = `1`
Then number of elements found by `relative(id(block1)>>above(id(block4)))` is = `1`
Then number of elements found by `relative(className(item4)>>below(id(block1)))` is = `1`
Then number of elements found by `relative(xpath(//div)>>toLeftOf(id(block2)))` is = `3`

Scenario: Verify easy directions negative
Given I am on page with URL `${vividus-test-site-url}/relativeLocator.html`
Then number of elements found by `id(block1)` is = `1`
Then number of elements found by `id(block2)` is = `1`
Then number of elements found by `relative(id(block1)>>toRightOf(id(block2)))` is = `0`

Scenario: Verify complex success
Given I am on page with URL `${vividus-test-site-url}/relativeLocator.html`
Then number of elements found by `relative(id(block5)>>toLeftOf(id(block7))>>toLeftOf(id(block9))>>toRightOf(id(block3))>>toRightOf(id(block4))>>above(id(block8))>>above(id(block9))>>below(id(block2))>>below(id(block7)))` is = `1`
Then number of elements found by `relative(id(block5)>>toLeftOf(id(block7)))` is = `1`

Scenario: Verify near
Given I am on page with URL `${vividus-test-site-url}/relativeLocator.html`
Then number of elements found by `relative(id(block1)>>near(id(block2)))` is = `1`
Then number of elements found by `relative(id(block1)>>near(id(block4)))` is = `1`
Then number of elements found by `relative(id(block1)>>near(id(block7)))` is = `0`
Then number of elements found by `relative(id(block1)>>near(id(block3)))` is = `0`
Then number of elements found by `relative(id(block1)>>near200px(id(block7)))` is = `1`
Then number of elements found by `relative(id(block1)>>near200px(id(block3)))` is = `1`

Scenario: Verify locator types
Given I am on page with URL `${vividus-test-site-url}/relativeLocator.html`
Then number of elements found by `relative(id(block1)>>toLeftOf(className(item2)))` is = `1`
Then number of elements found by `relative(className(item1)>>toLeftOf(cssSelector(.item2)))` is = `1`
Then number of elements found by `relative(xpath(//div[@id='block1'])>>toLeftOf(name(item2)))` is = `1`
Then number of elements found by `relative(id(block1)>>toLeftOf(caseInsensitiveText(TWO)))` is = `1`
Then number of elements found by `relative(id(block1)>>toLeftOf(caseSensitiveText(Two)))` is = `1`
Then number of elements found by `relative(id(block4)>>toLeftOf(linkText(Index)))` is = `1`
Then number of elements found by `relative(id(block4)>>toLeftOf(linkUrl(index.html)))` is = `1`
Then number of elements found by `relative(id(block4)>>toLeftOf(linkUrlPart(index)))` is = `1`
Then number of elements found by `relative(id(block8)>>toLeftOf(tagName(label)))` is = `1`

Scenario: Verify locator visible types and filters
Given I am on page with URL `${vividus-test-site-url}/relativeLocator.html`
Then number of elements found by `relative(xpath(//div)>>toRightOf(id(block2)))` is = `2`
Then number of elements found by `relative(xpath(//div)>>toRightOf(id(block2))):a` is = `3`
Then number of elements found by `relative(xpath(//div)>>toRightOf(id(block2))):i` is = `1`
Then number of elements found by `relative(xpath(//div)>>toRightOf(id(block2)))->filter.textPart(Seven)` is = `1`
Then number of elements found by `relative(xpath(//div)>>toRightOf(id(block2))):a->filter.textPart(e)` is = `2`
Then number of elements found by `relative(xpath(//div)>>above(xpath(//a)->filter.linkUrlPart(links)))->filter.attribute(class=item2)` is = `1`
Then number of elements found by `relative(xpath(//div)>>toLeftOf(xpath(//div):i->filter.attribute(id))>>toRightOf(id(block4)))` is = `3`
